package com.hmg.role.abac.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hmg.role.abac.permission.dto.AbacPolicySearchDto;
import com.hmg.role.abac.policy.dto.AbacPolicyDto;
import com.hmg.role.abac.policy.dto.DeleteBulkAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateBulkAbacPolicyDto;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemMapper;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemRepository;
import com.hmg.role.abac.resourceset.ResourceSet;
import com.hmg.role.abac.resourceset.ResourceSetRepository;
import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.resourceset.action.ResourceSetActionRepository;
import com.hmg.role.abac.resourceset.exceptions.ResourceSetNotFoundException;
import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.abac.scope.AbacScopeRepository;
import com.hmg.role.abac.userset.UserSet;
import com.hmg.role.abac.userset.UserSetRepository;
import com.hmg.role.abac.userset.exceptions.UserSetNotFoundException;
import com.hmg.role.admin.member.Member;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.rbac.policy.exceptions.ActionNotFoundException;
import com.hmg.role.rbac.policy.exceptions.PolicyAlreadyExistException;
import com.hmg.role.rbac.policy.exceptions.PolicyNotFoundException;
import com.hmg.role.rbac.policy.projections.PolicyItemProjection;
import com.hmg.role.rbac.scope.exceptions.ScopeNotFoundException;
import com.hmg.role.rbac.scope.interfaces.ScopeService;
import com.hmg.role.util.AuthorRequestScope;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AbacPolicyServiceImplTest {

    @Mock private AbacPolicyRepository policyRepository;
    @Mock private AbacPolicyItemRepository policyItemRepository;
    @Mock private ResourceSetActionRepository resourceSetActionRepository;
    @Mock private UserSetRepository userSetRepository;
    @Mock private ResourceSetRepository resourceSetRepository;
    @Mock private AbacScopeRepository scopeRepository;
    @Mock private AbacPolicyMapper abacPolicyMapper;
    @Mock private AbacPolicyItemMapper abacPolicyItemMapper;
    @Mock private ScopeService scopeService;
    @Mock private AuthorRequestScope authorRequestScope;

    @InjectMocks private AbacPolicyServiceImpl service;

    // Common test fixtures
    private Project project;
    private AbacScope scope;
    private ResourceSet resourceSet;
    private ResourceSetAction action;
    private UserSet userSet;
    private AbacPolicy policy;
    private AbacPolicyItem policyItem;

    @BeforeEach
    void setUp() {
        service.setAuthorRequestScope(authorRequestScope);

        project = new Project();
        project.setId(1L);
        project.setKey("proj-1");

        scope = new AbacScope();
        scope.setId(1L);
        scope.setKey("scope-1");
        scope.setProject(project);
        project.setDefaultScopeAbac(scope);

        resourceSet = new ResourceSet();
        resourceSet.setId(1L);
        resourceSet.setKey("rs-1");

        action = new ResourceSetAction();
        action.setId(1L);
        action.setActionName("read");
        action.setResourceSet(resourceSet);

        userSet = new UserSet();
        userSet.setId(1L);
        userSet.setKey("us-1");

        policy = new AbacPolicy();
        policy.setId(1L);
        policy.setKey("policy-1");
        policy.setProject(project);

        policyItem = new AbacPolicyItem();
        policyItem.setId(1L);
        policyItem.setPolicy(policy);
        policyItem.setEffect(Effect.ALLOW);
        policyItem.setUserSet(userSet);
        policyItem.setResourceSetAction(action);
        policyItem.setScope(scope);

        when(authorRequestScope.getProject()).thenReturn(project);
    }

    // ==================== createPolicy ====================

    @Test
    void createPolicy_success_returnsPolicyDto() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(List.of("scope-1"), project))
                .thenReturn(List.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(abacPolicyMapper.toPolicy(dto, project, "member-1", "member-1")).thenReturn(policy);
        when(policyRepository.save(policy)).thenReturn(policy);
        when(abacPolicyItemMapper.toPolicyItem(dto, action, scope, policy, userSet))
                .thenReturn(policyItem);
        when(policyItemRepository.saveAll(List.of(policyItem))).thenReturn(List.of(policyItem));

        var expectedDto = AbacPolicyDto.builder().key("policy-1").build();
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem))).thenReturn(expectedDto);

        var result = service.createPolicy(dto);

        assertNotNull(result);
        assertEquals("policy-1", result.key());
    }

    @Test
    void createPolicy_resourceSetNotFound_throwsResourceSetNotFoundException() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-missing")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(
                        List.of("rs-missing"), project))
                .thenReturn(List.of());

        assertThrows(ResourceSetNotFoundException.class, () -> service.createPolicy(dto));
    }

    @Test
    void createPolicy_policyAlreadyExists_throwsPolicyAlreadyExistException() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of(policy));

        assertThrows(PolicyAlreadyExistException.class, () -> service.createPolicy(dto));
    }

    @Test
    void createPolicy_actionNotFound_throwsActionNotFoundException() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("non-existent-action"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));

        assertThrows(ActionNotFoundException.class, () -> service.createPolicy(dto));
    }

    @Test
    void createPolicy_userSetNotFound_throwsUserSetNotFoundException() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-missing"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-missing"), project))
                .thenReturn(List.of());

        assertThrows(UserSetNotFoundException.class, () -> service.createPolicy(dto));
    }

    // ==================== createBulkPolicies ====================

    @Test
    void createBulkPolicies_multipleEntries_savesEachPolicy() {
        var dto1 =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();
        var dto2 =
                AbacPolicyDto.builder()
                        .key("policy-2")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.DENY)
                        .build();

        var policy2 = new AbacPolicy();
        policy2.setId(2L);
        policy2.setKey("policy-2");
        policy2.setProject(project);

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(
                        List.of("policy-1", "policy-2"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(List.of("scope-1"), project))
                .thenReturn(List.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(abacPolicyMapper.toPolicy(dto1, project, "member-1", "member-1")).thenReturn(policy);
        when(abacPolicyMapper.toPolicy(dto2, project, "member-1", "member-1")).thenReturn(policy2);
        when(policyRepository.save(policy)).thenReturn(policy);
        when(policyRepository.save(policy2)).thenReturn(policy2);
        when(abacPolicyItemMapper.toPolicyItem(dto1, action, scope, policy, userSet))
                .thenReturn(policyItem);
        when(abacPolicyItemMapper.toPolicyItem(dto2, action, scope, policy2, userSet))
                .thenReturn(policyItem);
        when(policyItemRepository.saveAll(any())).thenReturn(List.of(policyItem));

        var expectedDto1 = AbacPolicyDto.builder().key("policy-1").build();
        var expectedDto2 = AbacPolicyDto.builder().key("policy-2").build();
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem))).thenReturn(expectedDto1);
        when(abacPolicyMapper.toPolicyDto(policy2, List.of(policyItem))).thenReturn(expectedDto2);

        var result = service.createBulkPolicies(List.of(dto1, dto2));

        assertNotNull(result);
        assertEquals(2, result.results().size());
    }

    // ==================== getPolicyByKey ====================

    @Test
    void getPolicyByKey_found_returnsPolicyDto() {
        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy))
                .thenReturn(List.of(policyItem));

        var expectedDto = AbacPolicyDto.builder().key("policy-1").build();
        when(abacPolicyMapper.toPolicyDto(List.of(policyItem))).thenReturn(expectedDto);

        var result = service.getPolicyByKey("policy-1");

        assertNotNull(result);
        assertEquals("policy-1", result.key());
    }

    @Test
    void getPolicyByKey_notFound_throwsPolicyNotFoundException() {
        when(policyRepository.findByKeyAndProjectAndDeletedFalse("non-existent", project))
                .thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> service.getPolicyByKey("non-existent"));
    }

    // ==================== updatePolicy ====================

    @Test
    void updatePolicy_success_returnsPolicyDto() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.saveAll(any())).thenReturn(List.of(policyItem));
        when(policyRepository.save(policy)).thenReturn(policy);

        var expectedDto = AbacPolicyDto.builder().key("policy-1").build();
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem))).thenReturn(expectedDto);

        var result = service.updatePolicy("policy-1", updateDto);

        assertNotNull(result);
        assertEquals("policy-1", result.key());
        verify(policyItemRepository).findByPolicyAndDeletedFalse(policy);
    }

    @Test
    void updatePolicy_policyNotFound_throwsPolicyNotFoundException() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("non-existent", project))
                .thenReturn(Optional.empty());

        assertThrows(
                PolicyNotFoundException.class,
                () -> service.updatePolicy("non-existent", updateDto));
    }

    @Test
    void updatePolicy_resourceSetNotFound_throwsResourceSetNotFoundException() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-missing")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-missing", project))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceSetNotFoundException.class,
                () -> service.updatePolicy("policy-1", updateDto));
    }

    @Test
    void updatePolicy_actionNotFound_throwsActionNotFoundException() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("non-existent-action"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));

        assertThrows(
                ActionNotFoundException.class, () -> service.updatePolicy("policy-1", updateDto));
    }

    @Test
    void updatePolicy_userSetNotFound_throwsUserSetNotFoundException() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-missing"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-missing"), project))
                .thenReturn(List.of());

        assertThrows(
                UserSetNotFoundException.class, () -> service.updatePolicy("policy-1", updateDto));
    }

    // ==================== updateBulkPolicies ====================

    @Test
    void updateBulkPolicies_success_returnsPolicyDtos() {
        var updateDto =
                new UpdateBulkAbacPolicyDto(
                        "policy-1",
                        "scope-1",
                        null,
                        "rs-1",
                        List.of("read"),
                        List.of("us-1"),
                        Effect.ALLOW);

        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of(policy));
        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(List.of("scope-1"), project))
                .thenReturn(List.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.saveAll(any())).thenReturn(List.of(policyItem));
        when(policyRepository.save(policy)).thenReturn(policy);

        var expectedDto = AbacPolicyDto.builder().key("policy-1").build();
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem))).thenReturn(expectedDto);

        var result = service.updateBulkPolicies(List.of(updateDto));

        assertNotNull(result);
        assertEquals(1, result.results().size());
        assertEquals("policy-1", result.results().getFirst().key());
    }

    @Test
    void updateBulkPolicies_policyNotFound_throwsPolicyNotFoundException() {
        var updateDto =
                new UpdateBulkAbacPolicyDto(
                        "policy-missing",
                        "scope-1",
                        null,
                        "rs-1",
                        List.of("read"),
                        List.of("us-1"),
                        Effect.ALLOW);

        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(
                        List.of("policy-missing"), project))
                .thenReturn(List.of());

        assertThrows(
                PolicyNotFoundException.class,
                () -> service.updateBulkPolicies(List.of(updateDto)));
    }

    // ==================== deletePolicy ====================

    @Test
    void deletePolicy_success_softDeletesPolicyAndItems() {
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of(policy));
        when(policyRepository.saveAll(any())).thenReturn(List.of(policy));
        when(policyItemRepository.findByPolicyInAndDeletedFalse(any()))
                .thenReturn(List.of(policyItem));

        service.deletePolicy("policy-1");

        assertTrue(policy.isDeleted());
        verify(policyRepository).saveAll(any());
        verify(policyItemRepository).saveAll(any());
        assertTrue(policyItem.isDeleted());
    }

    @Test
    void deletePolicy_policyNotFound_throwsPolicyNotFoundException() {
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(
                        List.of("non-existent"), project))
                .thenReturn(List.of());

        assertThrows(PolicyNotFoundException.class, () -> service.deletePolicy("non-existent"));
    }

    // ==================== deleteBulkPolicies ====================

    @Test
    void deleteBulkPolicies_success_softDeletesAllPoliciesAndItems() {
        var deleteDto = new DeleteBulkAbacPolicyDto(List.of("policy-1"));

        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of(policy));
        when(policyRepository.saveAll(any())).thenReturn(List.of(policy));
        when(policyItemRepository.findByPolicyInAndDeletedFalse(any()))
                .thenReturn(List.of(policyItem));

        service.deleteBulkPolicies(deleteDto);

        verify(policyRepository).saveAll(any());
        verify(policyItemRepository).saveAll(any());
    }

    @Test
    void deleteBulkPolicies_someKeysNotFound_throwsPolicyNotFoundException() {
        var deleteDto = new DeleteBulkAbacPolicyDto(List.of("policy-1", "policy-missing"));

        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(
                        List.of("policy-1", "policy-missing"), project))
                .thenReturn(List.of(policy)); // only policy-1 found

        assertThrows(PolicyNotFoundException.class, () -> service.deleteBulkPolicies(deleteDto));
    }

    // ==================== getPolicies ====================

    @Test
    void getPolicies_success_withNonBlankFilters_returnsPolicyDtos() {
        var searchDto = new AbacPolicySearchDto();
        searchDto.setScopeKey("scope-1");
        searchDto.setUserSetKeyLike("us");
        searchDto.setResourceSetKeyLike("rs");

        var pageable = PageRequest.of(0, 10);
        var projection = mock(PolicyItemProjection.class);
        when(projection.getPolicyItemIdsCsv()).thenReturn("1");
        var queryPage = new PageImpl<PolicyItemProjection>(List.of(projection), pageable, 1);

        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(policyItemRepository.findIdsByCriteria("us", "rs", project, scope, pageable))
                .thenReturn(queryPage);
        when(policyItemRepository.findByIdIn(List.of(1L))).thenReturn(List.of(policyItem));

        var expectedDto = AbacPolicyDto.builder().key("policy-1").build();
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem))).thenReturn(expectedDto);

        var result = service.getPolicies(searchDto);

        assertNotNull(result);
        assertEquals(1, result.results().size());
    }

    @Test
    void getPolicies_success_withBlankFilters_returnsPolicyDtos() {
        var searchDto = new AbacPolicySearchDto();
        searchDto.setScopeKey("scope-1");

        var pageable = PageRequest.of(0, 10);
        var projection = mock(PolicyItemProjection.class);
        when(projection.getPolicyItemIdsCsv()).thenReturn("1");
        var queryPage = new PageImpl<PolicyItemProjection>(List.of(projection), pageable, 1);

        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(policyItemRepository.findIdsByCriteria(null, null, project, scope, pageable))
                .thenReturn(queryPage);
        when(policyItemRepository.findByIdIn(List.of(1L))).thenReturn(List.of(policyItem));

        var expectedDto = AbacPolicyDto.builder().key("policy-1").build();
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem))).thenReturn(expectedDto);

        var result = service.getPolicies(searchDto);

        assertNotNull(result);
        assertEquals(1, result.results().size());
    }

    @Test
    void getPolicies_scopeNotFound_throwsScopeNotFoundException() {
        var searchDto = new AbacPolicySearchDto();
        searchDto.setScopeKey("non-existent");

        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("non-existent", project))
                .thenReturn(Optional.empty());

        assertThrows(ScopeNotFoundException.class, () -> service.getPolicies(searchDto));
    }

    // ==================== getPolicyByKey (additional) ====================

    @Test
    void getPolicyByKey_emptyItems_throwsPolicyNotFoundException() {
        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy)).thenReturn(List.of());

        assertThrows(PolicyNotFoundException.class, () -> service.getPolicyByKey("policy-1"));
    }

    // ==================== createPolicy (additional) ====================

    @Test
    void createPolicy_scopeNotFound_throwsScopeNotFoundException() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-missing")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        // scopeByKey cache calls findByKeyInAndProjectAndDeletedFalse — not mocked → returns empty
        // → ScopeNotFoundException

        assertThrows(ScopeNotFoundException.class, () -> service.createPolicy(dto));
    }

    @Test
    void createPolicy_userSetNotFoundDuringMapping_throwsUserSetNotFoundException() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        // userSetByKey cache returns empty → checkUserSet throws UserSetNotFoundException
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of());

        assertThrows(UserSetNotFoundException.class, () -> service.createPolicy(dto));
    }

    @Test
    void createPolicy_duplicateResourceSetActions_usesFirstAction() {
        var action2 = new ResourceSetAction();
        action2.setId(2L);
        action2.setActionName("read");
        action2.setResourceSet(resourceSet);

        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action, action2));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(List.of("scope-1"), project))
                .thenReturn(List.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(abacPolicyMapper.toPolicy(dto, project, "member-1", "member-1")).thenReturn(policy);
        when(policyRepository.save(policy)).thenReturn(policy);
        when(abacPolicyItemMapper.toPolicyItem(dto, action, scope, policy, userSet))
                .thenReturn(policyItem);
        when(policyItemRepository.saveAll(List.of(policyItem))).thenReturn(List.of(policyItem));

        var expectedDto = AbacPolicyDto.builder().key("policy-1").build();
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem))).thenReturn(expectedDto);

        var result = service.createPolicy(dto);

        assertNotNull(result);
        assertEquals("policy-1", result.key());
    }

    // ==================== updatePolicy (additional) ====================

    @Test
    void updatePolicy_scopeNotFound_throwsScopeNotFoundException() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-missing")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-missing", project))
                .thenReturn(Optional.empty());

        assertThrows(
                ScopeNotFoundException.class, () -> service.updatePolicy("policy-1", updateDto));
    }

    @Test
    void updatePolicy_withExistingItems_softDeletesOldItems() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy))
                .thenReturn(List.of(policyItem));
        when(policyItemRepository.saveAll(any())).thenReturn(List.of(policyItem));
        when(policyRepository.save(policy)).thenReturn(policy);

        var expectedDto = AbacPolicyDto.builder().key("policy-1").build();
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem))).thenReturn(expectedDto);

        service.updatePolicy("policy-1", updateDto);

        assertTrue(policyItem.isDeleted());
    }

    // ==================== updateBulkPolicies (additional) ====================

    @Test
    void updateBulkPolicies_scopeNotFound_throwsScopeNotFoundException() {
        var updateDto =
                new UpdateBulkAbacPolicyDto(
                        "policy-1",
                        "scope-missing",
                        null,
                        "rs-1",
                        List.of("read"),
                        List.of("us-1"),
                        Effect.ALLOW);

        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of(policy));
        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        // scopeByKey cache calls findByKeyInAndProjectAndDeletedFalse — not mocked → returns empty
        // → ScopeNotFoundException

        assertThrows(
                ScopeNotFoundException.class, () -> service.updateBulkPolicies(List.of(updateDto)));
    }

    @Test
    void updateBulkPolicies_withExistingItems_softDeletesOldItems() {
        var updateDto =
                new UpdateBulkAbacPolicyDto(
                        "policy-1",
                        "scope-1",
                        null,
                        "rs-1",
                        List.of("read"),
                        List.of("us-1"),
                        Effect.ALLOW);

        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of(policy));
        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(List.of("scope-1"), project))
                .thenReturn(List.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy))
                .thenReturn(List.of(policyItem));
        when(policyItemRepository.saveAll(any())).thenReturn(List.of(policyItem));
        when(policyRepository.save(policy)).thenReturn(policy);

        var expectedDto = AbacPolicyDto.builder().key("policy-1").build();
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem))).thenReturn(expectedDto);

        service.updateBulkPolicies(List.of(updateDto));

        assertTrue(policyItem.isDeleted());
    }

    @Test
    void updateBulkPolicies_userSetNotFoundDuringMapping_throwsUserSetNotFoundException() {
        var updateDto =
                new UpdateBulkAbacPolicyDto(
                        "policy-1",
                        "scope-1",
                        null,
                        "rs-1",
                        List.of("read"),
                        List.of("us-1"),
                        Effect.ALLOW);

        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of(policy));
        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        // userSetByKey cache returns empty → checkUserSet throws UserSetNotFoundException
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of());

        assertThrows(
                UserSetNotFoundException.class,
                () -> service.updateBulkPolicies(List.of(updateDto)));
    }

    // ==================== getProject (null path) ====================

    @Test
    void getProject_nullProject_fallsBackToMemberProject() {
        var member = new Member();
        member.setProject(project);
        when(authorRequestScope.getProject()).thenReturn(null);
        when(authorRequestScope.getMember()).thenReturn(member);
        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> service.getPolicyByKey("policy-1"));
    }

    // ==================== mergePolicyItems / validateNoDuplicatePolicyItems ====================

    @Test
    void mergePolicyItems_existingItemMatchesRequest_survivesWithUpdatedEffect() {
        policyItem.setScopeKey("scope-1");
        policyItem.setUserSetKey("us-1");
        policyItem.setResourceSetKey("rs-1");
        policyItem.setActionName("read");
        policyItem.setEffect(Effect.ALLOW);

        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.DENY)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy))
                .thenReturn(List.of(policyItem));
        when(policyItemRepository.saveAll(any())).thenReturn(List.of(policyItem));
        when(policyRepository.save(policy)).thenReturn(policy);
        when(abacPolicyMapper.toPolicyDto(policy, List.of(policyItem)))
                .thenReturn(AbacPolicyDto.builder().key("policy-1").build());

        service.updatePolicy("policy-1", updateDto);

        assertEquals(Effect.DENY, policyItem.getEffect());
        assertFalse(policyItem.isDeleted());
        verify(policyItemRepository, times(1)).saveAll(any());
        verify(policyItemRepository, never())
                .existsConflictingPolicyItem(any(), any(), any(), any(), any(), any());
    }

    @Test
    void mergePolicyItems_noExistingItems_createsNewItemsAndCallsConflictCheck() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy)).thenReturn(List.of());
        when(policyItemRepository.existsConflictingPolicyItem(
                        any(), any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(policyItemRepository.saveAll(any())).thenReturn(List.of());
        when(policyRepository.save(policy)).thenReturn(policy);
        when(abacPolicyMapper.toPolicyDto(policy, List.of()))
                .thenReturn(AbacPolicyDto.builder().key("policy-1").build());

        service.updatePolicy("policy-1", updateDto);

        verify(policyItemRepository, times(2)).saveAll(any());
        verify(policyItemRepository, times(1))
                .existsConflictingPolicyItem(any(), any(), any(), any(), any(), any());
    }

    @Test
    void mergePolicyItems_conflictingPolicyItem_throwsDuplicateAbacPolicyItemException() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy)).thenReturn(List.of());
        when(policyItemRepository.existsConflictingPolicyItem(
                        any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(policyItemRepository.saveAll(any())).thenReturn(List.of());

        assertThrows(
                DuplicateAbacPolicyItemException.class,
                () -> service.updatePolicy("policy-1", updateDto));
    }

    @Test
    void validateNoDuplicatePolicyItems_emptyUserSets_skipsConflictCheck() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of())
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of(), project))
                .thenReturn(List.of());
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(List.of("scope-1"), project))
                .thenReturn(List.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(abacPolicyMapper.toPolicy(dto, project, "member-1", "member-1")).thenReturn(policy);
        when(policyRepository.save(policy)).thenReturn(policy);
        when(policyItemRepository.saveAll(any())).thenReturn(List.of());
        when(abacPolicyMapper.toPolicyDto(policy, List.of()))
                .thenReturn(AbacPolicyDto.builder().key("policy-1").build());

        service.createPolicy(dto);

        verify(policyItemRepository, never())
                .existsConflictingPolicyItem(any(), any(), any(), any(), any(), any());
    }

    @Test
    void validateNoDuplicatePolicyItems_emptyActions_skipsConflictCheck() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of())
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(List.of("scope-1"), project))
                .thenReturn(List.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(abacPolicyMapper.toPolicy(dto, project, "member-1", "member-1")).thenReturn(policy);
        when(policyRepository.save(policy)).thenReturn(policy);
        when(policyItemRepository.saveAll(any())).thenReturn(List.of());
        when(abacPolicyMapper.toPolicyDto(policy, List.of()))
                .thenReturn(AbacPolicyDto.builder().key("policy-1").build());

        service.createPolicy(dto);

        verify(policyItemRepository, never())
                .existsConflictingPolicyItem(any(), any(), any(), any(), any(), any());
    }

    @Test
    void createPolicy_conflictingPolicyItem_throwsDuplicateAbacPolicyItemException() {
        var dto =
                AbacPolicyDto.builder()
                        .key("policy-1")
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of(resourceSet));
        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of());
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyInAndProjectAndDeletedFalse(List.of("scope-1"), project))
                .thenReturn(List.of(scope));
        when(policyItemRepository.existsConflictingPolicyItem(
                        any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(DuplicateAbacPolicyItemException.class, () -> service.createPolicy(dto));
    }

    @Test
    void mergePolicyItems_userSetNotFoundDuringMerge_throwsUserSetNotFoundException() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet))
                .thenReturn(List.of());
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy)).thenReturn(List.of());
        when(policyItemRepository.saveAll(any())).thenReturn(List.of());

        assertThrows(
                UserSetNotFoundException.class, () -> service.updatePolicy("policy-1", updateDto));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mergePolicyItems_sameUserSetMultipleNewActions_deduplicatesUserSetInValidation() {
        var action2 = new ResourceSetAction();
        action2.setId(2L);
        action2.setActionName("write");
        action2.setResourceSet(resourceSet);

        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read", "write"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action, action2));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy)).thenReturn(List.of());
        when(policyItemRepository.existsConflictingPolicyItem(
                        any(), any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(policyItemRepository.saveAll(any())).thenReturn(List.of());
        when(policyRepository.save(policy)).thenReturn(policy);
        when(abacPolicyMapper.toPolicyDto(policy, List.of()))
                .thenReturn(AbacPolicyDto.builder().key("policy-1").build());

        service.updatePolicy("policy-1", updateDto);

        ArgumentCaptor<List<UserSet>> userSetsCaptor = ArgumentCaptor.forClass(List.class);
        verify(policyItemRepository)
                .existsConflictingPolicyItem(
                        any(), any(), userSetsCaptor.capture(), any(), any(), any());
        assertEquals(1, userSetsCaptor.getValue().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void mergePolicyItems_multipleNewUserSetsSameAction_deduplicatesActionInValidation() {
        var userSet2 = new UserSet();
        userSet2.setId(2L);
        userSet2.setKey("us-2");

        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("read"))
                        .userSets(List.of("us-1", "us-2"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(action));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(
                        List.of("us-1", "us-2"), project))
                .thenReturn(List.of(userSet, userSet2));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy)).thenReturn(List.of());
        when(policyItemRepository.existsConflictingPolicyItem(
                        any(), any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(policyItemRepository.saveAll(any())).thenReturn(List.of());
        when(policyRepository.save(policy)).thenReturn(policy);
        when(abacPolicyMapper.toPolicyDto(policy, List.of()))
                .thenReturn(AbacPolicyDto.builder().key("policy-1").build());

        service.updatePolicy("policy-1", updateDto);

        ArgumentCaptor<List<String>> actionNamesCaptor = ArgumentCaptor.forClass(List.class);
        verify(policyItemRepository)
                .existsConflictingPolicyItem(
                        any(), actionNamesCaptor.capture(), any(), any(), any(), any());
        assertEquals(1, actionNamesCaptor.getValue().size());
        assertEquals("read", actionNamesCaptor.getValue().get(0));
    }

    // ==================== parseItemIds (empty page branch) ====================

    @Test
    void getPolicies_emptyPage_returnsEmptyResult() {
        var searchDto = new AbacPolicySearchDto();
        searchDto.setScopeKey("scope-1");

        var pageable = PageRequest.of(0, 10);
        var emptyPage = new PageImpl<PolicyItemProjection>(List.of(), pageable, 0);

        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(policyItemRepository.findIdsByCriteria(null, null, project, scope, pageable))
                .thenReturn(emptyPage);

        var result = service.getPolicies(searchDto);

        assertNotNull(result);
        assertEquals(0, result.results().size());
    }

    // ==================== mergePolicyItems (empty actionMap branch) ====================

    @Test
    void mergePolicyItems_emptyActionMap_softDeletesAllExistingItems() {
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of())
                        .userSets(List.of("us-1"))
                        .effect(Effect.ALLOW)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of());
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy))
                .thenReturn(List.of(policyItem));
        when(policyItemRepository.saveAll(any())).thenReturn(List.of(policyItem));
        when(policyRepository.save(policy)).thenReturn(policy);
        when(abacPolicyMapper.toPolicyDto(policy, List.of()))
                .thenReturn(AbacPolicyDto.builder().key("policy-1").build());

        service.updatePolicy("policy-1", updateDto);

        assertTrue(policyItem.isDeleted());
        verify(policyItemRepository, times(1)).saveAll(any());
        verify(policyItemRepository, never())
                .existsConflictingPolicyItem(any(), any(), any(), any(), any(), any());
    }

    // ==================== saveAbacPolicyProcess (resourceSet not in cache) ====================

    @Test
    void updateBulkPolicies_resourceSetNotFound_throwsResourceSetNotFoundException() {
        var updateDto =
                new UpdateBulkAbacPolicyDto(
                        "policy-1",
                        "scope-1",
                        null,
                        "rs-1",
                        List.of("read"),
                        List.of("us-1"),
                        Effect.ALLOW);

        when(policyRepository.findByKeyInAndProjectAndDeletedFalse(List.of("policy-1"), project))
                .thenReturn(List.of(policy));
        when(resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("rs-1"), project))
                .thenReturn(List.of());

        assertThrows(
                ResourceSetNotFoundException.class,
                () -> service.updateBulkPolicies(List.of(updateDto)));
    }

    // ==================== mergePolicyItems (RS actions updated; existing items survive)
    // ====================

    @Test
    void mergePolicyItems_existingItemsMatchRequestedActions_survivorsPreservedNoConflictCheck() {
        var actionOne = new ResourceSetAction();
        actionOne.setId(1L);
        actionOne.setActionName("one");
        actionOne.setResourceSet(resourceSet);

        var actionTwo = new ResourceSetAction();
        actionTwo.setId(2L);
        actionTwo.setActionName("two");
        actionTwo.setResourceSet(resourceSet);

        var actionFour = new ResourceSetAction();
        actionFour.setId(4L);
        actionFour.setActionName("four");
        actionFour.setResourceSet(resourceSet);

        var itemOne = new AbacPolicyItem();
        itemOne.setId(10L);
        itemOne.setPolicy(policy);
        itemOne.setEffect(Effect.ALLOW);
        itemOne.setScopeKey("scope-1");
        itemOne.setUserSetKey("us-1");
        itemOne.setResourceSetKey("rs-1");
        itemOne.setActionName("one");

        var itemTwo = new AbacPolicyItem();
        itemTwo.setId(11L);
        itemTwo.setPolicy(policy);
        itemTwo.setEffect(Effect.ALLOW);
        itemTwo.setScopeKey("scope-1");
        itemTwo.setUserSetKey("us-1");
        itemTwo.setResourceSetKey("rs-1");
        itemTwo.setActionName("two");

        // RS now has actions one/two/four (three was removed, four was added)
        // Update requests only one and two — four is in the actionMap but not requested
        var updateDto =
                UpdateAbacPolicyDto.builder()
                        .scope("scope-1")
                        .resourceSet("rs-1")
                        .actions(List.of("one", "two"))
                        .userSets(List.of("us-1"))
                        .effect(Effect.DENY)
                        .build();

        when(policyRepository.findByKeyAndProjectAndDeletedFalse("policy-1", project))
                .thenReturn(Optional.of(policy));
        when(resourceSetRepository.findByKeyAndProjectAndDeletedFalse("rs-1", project))
                .thenReturn(Optional.of(resourceSet));
        when(resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSet))
                .thenReturn(List.of(actionOne, actionTwo, actionFour));
        when(userSetRepository.findByKeyInAndProjectAndDeletedFalse(List.of("us-1"), project))
                .thenReturn(List.of(userSet));
        when(scopeRepository.findByKeyAndProjectAndDeletedFalse("scope-1", project))
                .thenReturn(Optional.of(scope));
        when(authorRequestScope.getMemberKey()).thenReturn("member-1");
        when(policyItemRepository.findByPolicyAndDeletedFalse(policy))
                .thenReturn(List.of(itemOne, itemTwo));
        when(policyItemRepository.saveAll(any())).thenReturn(List.of(itemOne, itemTwo));
        when(policyRepository.save(policy)).thenReturn(policy);
        when(abacPolicyMapper.toPolicyDto(policy, List.of(itemOne, itemTwo)))
                .thenReturn(AbacPolicyDto.builder().key("policy-1").build());

        service.updatePolicy("policy-1", updateDto);

        assertFalse(itemOne.isDeleted());
        assertFalse(itemTwo.isDeleted());
        assertEquals(Effect.DENY, itemOne.getEffect());
        assertEquals(Effect.DENY, itemTwo.getEffect());
        // one saveAll for existing items (both survive); no second saveAll since toCreate is empty
        verify(policyItemRepository, times(1)).saveAll(any());
        verify(policyItemRepository, never())
                .existsConflictingPolicyItem(any(), any(), any(), any(), any(), any());
    }
}
