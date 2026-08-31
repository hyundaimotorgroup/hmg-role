package com.hmg.role.abac.scope;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hmg.role.abac.policy.AbacPolicyItem;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemRepository;
import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.admin.member.Member;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.scope.dto.CreateScopeDto;
import com.hmg.role.rbac.scope.dto.ScopeDto;
import com.hmg.role.rbac.scope.dto.UpdateScopeDto;
import com.hmg.role.rbac.scope.exceptions.ScopeAlreadyExistException;
import com.hmg.role.rbac.scope.exceptions.ScopeBeingUsedException;
import com.hmg.role.rbac.scope.exceptions.ScopeNotFoundException;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.Cache;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestDto;
import com.hmg.role.util.exceptions.BadRequestException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class AbacScopeServiceImplTest {

    @Mock private AbacScopeRepository scopeRepository;
    @Mock private AbacPolicyItemRepository policyItemRepository;
    @Mock private AuditService auditService;
    @Mock private AuthorRequestScope authorRequestScope;
    @Mock private AbacScopeMapper scopeMapper;

    @InjectMocks private AbacScopeServiceImpl service;

    private Project project;
    private AbacScope scope;

    @BeforeEach
    void setUp() {
        service.setAuthorRequestScope(authorRequestScope);

        project = new Project();
        project.setId(1L);
        project.setKey("proj-1");

        scope = new AbacScope();
        scope.setId(1L);
        scope.setKey("scope-1");
        scope.setName("Scope One");
        scope.setProject(project);
    }

    // ==================== create ====================

    @Test
    void create_success_savesAndReturnsScopeDto() {
        var createDto = CreateScopeDto.builder().key("scope-1").name("Scope One").build();
        var expectedDto = new ScopeDto("scope-1", "Scope One");

        when(authorRequestScope.getProject()).thenReturn(project);
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(scopeRepository.findByKeyOrNameAndProjectAndDeletedFalse(
                        "scope-1", "Scope One", project))
                .thenReturn(Optional.empty());
        when(scopeMapper.toScope(createDto, project, "member-1", "member-1")).thenReturn(scope);
        when(scopeRepository.save(scope)).thenReturn(scope);
        when(scopeMapper.toScopeDto(scope)).thenReturn(expectedDto);

        var result = service.create(createDto);

        assertNotNull(result);
        assertEquals("scope-1", result.key());
        verify(scopeRepository).save(scope);
    }

    @Test
    void create_projectNullFallsBackToMember_savesScope() {
        var createDto = CreateScopeDto.builder().key("scope-1").name("Scope One").build();
        var expectedDto = new ScopeDto("scope-1", "Scope One");

        Member member = new Member();
        member.setKey("member-1");
        member.setProject(project);

        when(authorRequestScope.getProject()).thenReturn(null);
        when(authorRequestScope.getMember()).thenReturn(member);
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(scopeRepository.findByKeyOrNameAndProjectAndDeletedFalse(
                        "scope-1", "Scope One", project))
                .thenReturn(Optional.empty());
        when(scopeMapper.toScope(createDto, project, "member-1", "member-1")).thenReturn(scope);
        when(scopeRepository.save(scope)).thenReturn(scope);
        when(scopeMapper.toScopeDto(scope)).thenReturn(expectedDto);

        var result = service.create(createDto);

        assertNotNull(result);
        assertEquals("scope-1", result.key());
    }

    @Test
    void create_keyConflict_throwsScopeAlreadyExistExceptionWithKeyType() {
        var createDto = CreateScopeDto.builder().key("scope-1").name("Different Name").build();

        AbacScope existing = new AbacScope();
        existing.setKey("scope-1");
        existing.setName("Other Name"); // name differs → KEY conflict

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyOrNameAndProjectAndDeletedFalse(
                        "scope-1", "Different Name", project))
                .thenReturn(Optional.of(existing));

        var ex = assertThrows(ScopeAlreadyExistException.class, () -> service.create(createDto));
        assertEquals("KEY", ex.getConflictType());
    }

    @Test
    void create_nameConflict_throwsScopeAlreadyExistExceptionWithNameType() {
        var createDto = CreateScopeDto.builder().key("new-key").name("Scope One").build();

        AbacScope existing = new AbacScope();
        existing.setKey("other-key"); // key differs → NAME conflict
        existing.setName("Scope One");

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyOrNameAndProjectAndDeletedFalse(
                        "new-key", "Scope One", project))
                .thenReturn(Optional.of(existing));

        var ex = assertThrows(ScopeAlreadyExistException.class, () -> service.create(createDto));
        assertEquals("NAME", ex.getConflictType());
    }

    @Test
    void create_bothKeyAndNameConflict_throwsScopeAlreadyExistExceptionWithBothType() {
        var createDto = CreateScopeDto.builder().key("scope-1").name("Scope One").build();

        AbacScope existing = new AbacScope();
        existing.setKey("scope-1"); // key matches
        existing.setName("Scope One"); // name also matches

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyOrNameAndProjectAndDeletedFalse(
                        "scope-1", "Scope One", project))
                .thenReturn(Optional.of(existing));

        var ex = assertThrows(ScopeAlreadyExistException.class, () -> service.create(createDto));
        assertEquals("BOTH", ex.getConflictType());
    }

    // ==================== getByKey ====================

    @Test
    void getByKey_scopeExists_returnsScopeDto() {
        var expectedDto = new ScopeDto("scope-1", "Scope One");

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(scopeMapper.toScopeDto(scope)).thenReturn(expectedDto);

        var result = service.getByKey("scope-1");

        assertEquals("scope-1", result.key());
    }

    @Test
    void getByKey_scopeNotFound_throwsScopeNotFoundException() {
        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("missing", project))
                .thenReturn(Optional.empty());

        assertThrows(ScopeNotFoundException.class, () -> service.getByKey("missing"));
    }

    // ==================== getAll ====================

    @Test
    void getAll_returnsPaginatedListOfScopeDtos() {
        var pageRequestDto = new PageRequestDto();
        var pageable = pageRequestDto.pageRequest();
        var page = new PageImpl<>(List.of(scope), pageable, 1);

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByProjectAndDeletedIsFalseOrderByNameAsc(project, pageable))
                .thenReturn(page);

        ListResponseDto<ScopeDto> result = service.getAll(pageRequestDto);

        assertNotNull(result);
        assertEquals(1, result.results().size());
        assertEquals("scope-1", result.results().get(0).key());
        assertEquals("Scope One", result.results().get(0).name());
    }

    // ==================== update ====================

    @Test
    void update_success_returnsUpdatedScopeDto() {
        var updateDto = UpdateScopeDto.builder().name("New Name").build();
        var updatedScope = new AbacScope();
        updatedScope.setKey("scope-1");
        updatedScope.setName("New Name");
        var expectedDto = new ScopeDto("scope-1", "New Name");

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByNameAndProjectAndDeletedFalse("New Name", project))
                .thenReturn(Optional.empty());
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(scopeMapper.toScope(scope, updateDto, "member-1")).thenReturn(updatedScope);
        when(scopeRepository.save(updatedScope)).thenReturn(updatedScope);
        when(scopeMapper.toScopeDto(updatedScope)).thenReturn(expectedDto);

        var result = service.update("scope-1", updateDto);

        assertEquals("New Name", result.name());
        verify(scopeRepository).save(updatedScope);
    }

    @Test
    void update_nameAlreadyExists_throwsScopeAlreadyExistException() {
        var updateDto = UpdateScopeDto.builder().name("Existing Name").build();

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByNameAndProjectAndDeletedFalse("Existing Name", project))
                .thenReturn(Optional.of(scope));

        assertThrows(ScopeAlreadyExistException.class, () -> service.update("scope-1", updateDto));
    }

    @Test
    void update_scopeNotFound_throwsScopeNotFoundException() {
        var updateDto = UpdateScopeDto.builder().name("New Name").build();

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByNameAndProjectAndDeletedFalse("New Name", project))
                .thenReturn(Optional.empty());
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("missing", project))
                .thenReturn(Optional.empty());

        assertThrows(ScopeNotFoundException.class, () -> service.update("missing", updateDto));
    }

    @Test
    void update_secondExistsScopeNameCheckTriggered_throwsScopeAlreadyExistException() {
        // Covers the second existsScopeName guard (lines 107-109) by returning
        // empty on the first call and present on the second call via mock chaining.
        var updateDto = UpdateScopeDto.builder().name("Name").build();

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByNameAndProjectAndDeletedFalse("Name", project))
                .thenReturn(Optional.empty()) // first guard passes
                .thenReturn(Optional.of(scope)); // second guard triggers
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("scope-1", project))
                .thenReturn(Optional.of(scope));

        assertThrows(ScopeAlreadyExistException.class, () -> service.update("scope-1", updateDto));
    }

    // ==================== deleteByKey ====================

    @Test
    void deleteByKey_success_softDeletesScopeWithTimestampedKey() {
        AbacScope defaultScope = new AbacScope();
        defaultScope.setKey("default-scope");

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(policyItemRepository.existsByScopeAndProject(scope, project)).thenReturn(false);
        when(authorRequestScope.getDefaultScopeAbac()).thenReturn(new Cache<>(() -> defaultScope));

        service.deleteByKey("scope-1");

        assertTrue(scope.isDeleted());
        assertTrue(scope.getKey().startsWith("deleted-"));
        verify(scopeRepository).save(scope);
    }

    @Test
    void deleteByKey_scopeNotFound_throwsScopeNotFoundException() {
        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("missing", project))
                .thenReturn(Optional.empty());

        assertThrows(ScopeNotFoundException.class, () -> service.deleteByKey("missing"));
    }

    @Test
    void deleteByKey_isDefaultScope_throwsScopeBeingUsedException() {
        // The scope being deleted IS the project's default scope
        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(policyItemRepository.existsByScopeAndProject(scope, project)).thenReturn(false);
        when(authorRequestScope.getDefaultScopeAbac()).thenReturn(new Cache<>(() -> scope));

        assertThrows(ScopeBeingUsedException.class, () -> service.deleteByKey("scope-1"));
    }

    @Test
    void deleteByKey_hasPoliciesAndNoCascade_throwsScopeBeingUsedException() {
        AbacScope defaultScope = new AbacScope();
        defaultScope.setKey("default-scope");

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(policyItemRepository.existsByScopeAndProject(scope, project)).thenReturn(true);
        when(authorRequestScope.getDefaultScopeAbac()).thenReturn(new Cache<>(() -> defaultScope));

        assertThrows(ScopeBeingUsedException.class, () -> service.deleteByKey("scope-1"));
    }

    // ==================== deleteCascadeByKey ====================

    @Test
    void deleteCascadeByKey_noPolicyItems_softDeletesScopeOnly() {
        AbacScope defaultScope = new AbacScope();
        defaultScope.setKey("default-scope");

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(policyItemRepository.existsByScopeAndProject(scope, project)).thenReturn(false);
        when(authorRequestScope.getDefaultScopeAbac()).thenReturn(new Cache<>(() -> defaultScope));

        service.deleteCascadeByKey("scope-1");

        assertTrue(scope.isDeleted());
        verify(scopeRepository).save(scope);
        verify(policyItemRepository, never()).findByScopeAndPolicyDeletedFalse(any());
    }

    @Test
    void deleteCascadeByKey_withPolicyItems_softDeletesPolicyItemsThenScope() {
        AbacScope defaultScope = new AbacScope();
        defaultScope.setKey("default-scope");

        var policyItem = new AbacPolicyItem();
        policyItem.setId(1L);

        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(policyItemRepository.existsByScopeAndProject(scope, project)).thenReturn(true);
        when(authorRequestScope.getDefaultScopeAbac()).thenReturn(new Cache<>(() -> defaultScope));
        when(policyItemRepository.findByScopeAndPolicyDeletedFalse(scope))
                .thenReturn(List.of(policyItem));
        when(policyItemRepository.saveAll(List.of(policyItem))).thenReturn(List.of(policyItem));

        service.deleteCascadeByKey("scope-1");

        assertTrue(policyItem.isDeleted());
        assertTrue(scope.isDeleted());
        verify(policyItemRepository).saveAll(List.of(policyItem));
        verify(scopeRepository).save(scope);
    }

    @Test
    void deleteCascadeByKey_isDefaultScope_throwsScopeBeingUsedException() {
        when(authorRequestScope.getProject()).thenReturn(project);
        when(scopeRepository.findByKeyAndProjectAndDeletedIsFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(policyItemRepository.existsByScopeAndProject(scope, project)).thenReturn(false);
        when(authorRequestScope.getDefaultScopeAbac()).thenReturn(new Cache<>(() -> scope));

        assertThrows(ScopeBeingUsedException.class, () -> service.deleteCascadeByKey("scope-1"));
    }

    // ==================== validateScope ====================

    @Test
    void validateScope_existingKey_doesNotThrow() {
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));

        assertDoesNotThrow(() -> service.validateScope("scope-1", project));
    }

    @Test
    void validateScope_keyNotFound_throwsScopeNotFoundException() {
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("missing", project))
                .thenReturn(Optional.empty());

        assertThrows(ScopeNotFoundException.class, () -> service.validateScope("missing", project));
    }

    @Test
    void validateScope_blankKey_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () -> service.validateScope("", project));
    }

    @Test
    void validateScope_nullKey_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () -> service.validateScope(null, project));
    }

    // ==================== validateScopes ====================

    @Test
    void validateScopes_emptyList_skipsRepositoryCall() {
        assertDoesNotThrow(() -> service.validateScopes(List.of(), project));
        verifyNoInteractions(scopeRepository);
    }

    @Test
    void validateScopes_allKeysFound_doesNotThrow() {
        var keys = List.of("scope-1");
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(keys, project))
                .thenReturn(List.of(scope)); // scope-1 found

        assertDoesNotThrow(() -> service.validateScopes(keys, project));
    }

    @Test
    void validateScopes_someKeysMissing_throwsScopeNotFoundExceptionWithMissingKeys() {
        var keys = List.of("scope-1", "scope-2");
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(keys, project))
                .thenReturn(List.of(scope)); // only scope-1 found; scope-2 missing

        var ex =
                assertThrows(
                        ScopeNotFoundException.class, () -> service.validateScopes(keys, project));
        assertTrue(ex.getScopeKeys().contains("scope-2"));
    }

    // ==================== existsScopeKey ====================

    @Test
    void existsScopeKey_found_returnsTrue() {
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));

        assertTrue(service.existsScopeKey("scope-1", project));
    }

    @Test
    void existsScopeKey_notFound_returnsFalse() {
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("missing", project))
                .thenReturn(Optional.empty());

        assertFalse(service.existsScopeKey("missing", project));
    }

    // ==================== existsScopeName ====================

    @Test
    void existsScopeName_found_returnsTrue() {
        when(scopeRepository.findByNameAndProjectAndDeletedFalse("Scope One", project))
                .thenReturn(Optional.of(scope));

        assertTrue(service.existsScopeName("Scope One", project));
    }

    @Test
    void existsScopeName_notFound_returnsFalse() {
        when(scopeRepository.findByNameAndProjectAndDeletedFalse("Missing", project))
                .thenReturn(Optional.empty());

        assertFalse(service.existsScopeName("Missing", project));
    }

    // ==================== existsScopeKeys ====================

    @Test
    void existsScopeKeys_delegatesToRepositoryAndReturnsResult() {
        var keys = List.of("scope-1");
        when(scopeRepository.existsByKeyInAndProjectAndDeletedFalse(keys, project))
                .thenReturn(true);

        assertTrue(service.existsScopeKeys(keys, project));
    }

    // ==================== getByScopeKeys ====================

    @Test
    void getByScopeKeys_returnsMappedScopeDtoList() {
        var keys = List.of("scope-1");
        var expectedDto = new ScopeDto("scope-1", "Scope One");

        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(keys, project))
                .thenReturn(List.of(scope));
        when(scopeMapper.toScopeDto(scope)).thenReturn(expectedDto);

        var result = service.getByScopeKeys(keys, project);

        assertEquals(1, result.size());
        assertEquals("scope-1", result.get(0).key());
    }

    // ==================== unsupported operations ====================

    @Test
    void findByKeysAndThrowIfNotExists_throwsUnsupportedOperationException() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> service.findByKeysAndThrowIfNotExists(List.of("scope-1")));
    }

    @Test
    void findByScopeKeyInAndProjectAndDeletedFalse_throwsUnsupportedOperationException() {
        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        service.findByScopeKeyInAndProjectAndDeletedFalse(
                                List.of("scope-1"), project));
    }
}
