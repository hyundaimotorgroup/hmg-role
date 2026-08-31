package com.hmg.role.abac.permission;

import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.*;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildActionEffect;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildAttributes;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildAttributesWithNullValue;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildConditionOperand;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildPermissionRequest;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildPolicyItem;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildProject;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildResource;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildResourceActions;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildResourceResponse;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildResourceSet;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildResourceSetAction;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildResourceSetCondition;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildResourceSetOperand;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildResourceSetWithConditions;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildResourceSetWithParent;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildScope;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildUser;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildUserSet;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildUserSetCondition;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildUserSetOperand;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildUserSetWithConditions;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildUserSetWithParents;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hmg.role.abac.common.exceptions.AbacAttributeInvalidTypeException;
import com.hmg.role.abac.common.exceptions.AbacNullPermissionAttributeValue;
import com.hmg.role.abac.logicalexpression.ConditionEvaluationService;
import com.hmg.role.abac.logicalexpression.EvaluationResult;
import com.hmg.role.abac.logicalexpression.LogicalExpressionMapper;
import com.hmg.role.abac.permission.dto.AbacPermissionFlattenedResponseDto;
import com.hmg.role.abac.permission.dto.AbacPermissionResponseDto;
import com.hmg.role.abac.permission.util.AbacPermissionMockFactory;
import com.hmg.role.abac.policy.AbacPolicyItem;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemRepository;
import com.hmg.role.abac.resourceset.ResourceSet;
import com.hmg.role.abac.resourceset.ResourceSetRepository;
import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.abac.userset.UserSet;
import com.hmg.role.abac.userset.UserSetRepository;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.rbac.scope.interfaces.ScopeService;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.enums.ConditionGroupOperator;
import com.hmg.role.util.enums.ConditionOperator;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandPosition;
import com.hmg.role.util.enums.OperandType;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AbacPermissionServiceImplTest {

    private UserSetRepository userSetRepository;
    private ResourceSetRepository resourceSetRepository;
    private AbacPolicyItemRepository abacPolicyItemRepository;
    private AbacPermissionMapper abacPermissionMapper;
    private ConditionEvaluationService conditionEvaluationService;
    private LogicalExpressionMapper logicalExpressionMapper;
    private ScopeService scopeService;
    private AuthorRequestScope authorRequestScope;

    private AbacPermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        userSetRepository = AbacPermissionMockFactory.createUserSetRepository();
        resourceSetRepository = AbacPermissionMockFactory.createResourceSetRepository();
        abacPolicyItemRepository = AbacPermissionMockFactory.createAbacPolicyItemRepository();
        abacPermissionMapper = AbacPermissionMockFactory.createAbacPermissionMapper();
        conditionEvaluationService =
                AbacPermissionMockFactory.createConditionEvaluationServiceWithResult(true);
        logicalExpressionMapper = AbacPermissionMockFactory.createLogicalExpressionMapper();
        scopeService = AbacPermissionMockFactory.createScopeServiceWithExistsResult(true);
        authorRequestScope = AbacPermissionMockFactory.createAuthorRequestScope();

        rebuildService();
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(
                userSetRepository,
                resourceSetRepository,
                abacPolicyItemRepository,
                abacPermissionMapper,
                conditionEvaluationService,
                logicalExpressionMapper,
                scopeService,
                authorRequestScope);
    }

    private void rebuildService() {
        service =
                new AbacPermissionServiceImpl(
                        userSetRepository,
                        resourceSetRepository,
                        abacPolicyItemRepository,
                        abacPermissionMapper,
                        conditionEvaluationService,
                        logicalExpressionMapper,
                        scopeService);
        service.setAuthorRequestScope(authorRequestScope);
    }

    // ==================== Scope Guard Tests ====================

    @Test
    void getAllPermissions_userScopeAndResourceScopeMismatch_returnsDenyWithoutQuerying() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var user = buildUser("user1", "scope_a", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope_b", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        var resourceResponse = buildResourceResponse("res1", null, null);
        when(abacPermissionMapper.toResourceResponseDto(any(), any())).thenReturn(resourceResponse);

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY),
                "All effects must be DENY when user scope and resource scope mismatch");
        verify(userSetRepository, never()).findAllByAttributesAndProject(any(), any());
        verify(resourceSetRepository, never()).findByAttributesAndProject(any(), any());
        verify(scopeService, never()).existsScopeKey(any(), any());
        verify(abacPolicyItemRepository, never())
                .findAllByResourceActionsAndUserSetAndProject(any(), any(), any(), any(), any());
    }

    @Test
    void getAllPermissions_scopeNotFoundInProject_returnsDenyWithoutQueryingPolicyItems() {
        Project project = buildProject(1L, "proj1", "Project 1");
        scopeService = AbacPermissionMockFactory.createScopeServiceWithExistsResult(false);
        rebuildService();

        var user = buildUser("user1", "ghost_scope", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "ghost_scope", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read", "write"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        var resourceResponse = buildResourceResponse("res1", null, null);
        when(abacPermissionMapper.toResourceResponseDto(any(), any())).thenReturn(resourceResponse);

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY),
                "All effects must be DENY when scope does not exist in project");
        verify(userSetRepository, never()).findAllByAttributesAndProject(any(), any());
        verify(resourceSetRepository, never()).findByAttributesAndProject(any(), any());
        verify(abacPolicyItemRepository, never())
                .findAllByResourceActionsAndUserSetAndProject(any(), any(), any(), any(), any());
    }

    @Test
    void getAllPermissions_userAttributesHasNullValue_throwsNullPermissionAttributeValue() {
        // Map.of("key", null) is illegal in Java; Jackson produces a HashMap with null values
        // when a request body contains {"key": null}. Use buildAttributesWithNullValue to simulate.
        Project project = buildProject(1L, "proj1", "Project 1");
        var user = buildUser("user1", "scope1", buildAttributesWithNullValue("role"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);

        assertThrows(
                AbacNullPermissionAttributeValue.class, () -> service.getAllPermissions(request));
        verify(userSetRepository, never()).findAllByAttributesAndProject(any(), any());
        verify(resourceSetRepository, never()).findByAttributesAndProject(any(), any());
    }

    @Test
    void getAllPermissions_resourceAttributesHasNullValue_throwsNullPermissionAttributeValue() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributesWithNullValue("type"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);

        assertThrows(
                AbacNullPermissionAttributeValue.class, () -> service.getAllPermissions(request));
        verify(userSetRepository, never()).findAllByAttributesAndProject(any(), any());
        verify(resourceSetRepository, never()).findByAttributesAndProject(any(), any());
    }

    // ==================== Basic Permission Tests ====================

    @Test
    void getAllPermissions_withValidScopeAndMatchingPolicy_returnsAllow() {
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        var resourceAction = buildResourceSetAction(1L, "read", resourceSet);
        AbacPolicyItem policyItem =
                buildPolicyItem(1L, Effect.ALLOW, userSet, resourceAction, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of(policyItem));

        var actionEffect = buildActionEffect("user1", "us1", "read", Effect.ALLOW);
        var resourceResponse = buildResourceResponse("res1", "rs1", null);
        when(abacPermissionMapper.toActionEffectDto(policyItem)).thenReturn(actionEffect);
        when(abacPermissionMapper.toResourceResponseDto(any(), any())).thenReturn(resourceResponse);

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        var effects =
                result.results().stream().flatMap(r -> r.getActionEffects().stream()).toList();
        assertTrue(
                effects.stream()
                        .anyMatch(
                                ae ->
                                        ae.getEffect() == Effect.ALLOW
                                                && "read".equals(ae.getAction())),
                "READ must be ALLOW when a matching policy exists");
    }

    @Test
    void getAllPermissions_withValidScopeButNoPolicyItems_returnsDenyForAllActions() {
        Project project = buildProject(1L, "proj1", "Project 1");
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read", "write"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());

        var resourceResponse = buildResourceResponse("res1", null, null);
        when(abacPermissionMapper.toResourceResponseDto(any(), any())).thenReturn(resourceResponse);

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY),
                "All effects must be DENY when no policy items exist");
    }

    @Test
    void getAllPermissions_projectFromMember_resolvesPolicyCorrectly() {
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        var resourceAction = buildResourceSetAction(1L, "read", resourceSet);
        AbacPolicyItem policyItem =
                buildPolicyItem(1L, Effect.ALLOW, userSet, resourceAction, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        var member = AbacPermissionMockFactory.createMember(project);
        when(authorRequestScope.getProject()).thenReturn(null);
        when(authorRequestScope.getMember()).thenReturn(member);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of(policyItem));

        var actionEffect = buildActionEffect("user1", "us1", "read", Effect.ALLOW);
        var resourceResponse = buildResourceResponse("res1", "rs1", null);
        when(abacPermissionMapper.toActionEffectDto(policyItem)).thenReturn(actionEffect);
        when(abacPermissionMapper.toResourceResponseDto(any(), any())).thenReturn(resourceResponse);

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .anyMatch(ae -> ae.getEffect() == Effect.ALLOW));
    }

    @Test
    void getAllPermissionsFlattened_withValidRequest_returnsFlattenedPermissions() {
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        var resourceAction = buildResourceSetAction(1L, "read", resourceSet);
        AbacPolicyItem policyItem =
                buildPolicyItem(1L, Effect.ALLOW, userSet, resourceAction, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of(policyItem));

        var actionEffect = buildActionEffect("user1", "us1", "read", Effect.ALLOW);
        var resourceResponse = buildResourceResponse("res1", "rs1", null);
        when(abacPermissionMapper.toActionEffectDto(policyItem)).thenReturn(actionEffect);
        when(abacPermissionMapper.toResourceResponseDto(any(), any())).thenReturn(resourceResponse);
        when(abacPermissionMapper.toPermissionFlattenedResponseDtoStream(any()))
                .thenReturn(
                        Stream.of(
                                AbacPermissionFlattenedResponseDto.builder()
                                        .userSet("us1")
                                        .resourceSet("rs1")
                                        .action("read")
                                        .effect("ALLOW")
                                        .build()));

        ListResponseDto<AbacPermissionFlattenedResponseDto> result =
                service.getAllPermissionsFlattened(request);

        assertNotNull(result);
        assertFalse(result.results().isEmpty());
        assertEquals("ALLOW", result.results().get(0).getEffect());
    }

    // ==================== Multi-Action Tests ====================

    @Test
    void getAllPermissions_multipleActions_allCovered_returnsAllEffectsWithoutDroppingAny() {
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        var readAction = buildResourceSetAction(1L, "read", resourceSet);
        var writeAction = buildResourceSetAction(2L, "write", resourceSet);
        AbacPolicyItem readPolicy = buildPolicyItem(1L, Effect.ALLOW, userSet, readAction, scope);
        AbacPolicyItem writePolicy = buildPolicyItem(2L, Effect.ALLOW, userSet, writeAction, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read", "write"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of(readPolicy, writePolicy));

        when(abacPermissionMapper.toActionEffectDto(readPolicy))
                .thenReturn(buildActionEffect("user1", "us1", "read", Effect.ALLOW));
        when(abacPermissionMapper.toActionEffectDto(writePolicy))
                .thenReturn(buildActionEffect("user1", "us1", "write", Effect.ALLOW));
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        var effects =
                result.results().stream().flatMap(r -> r.getActionEffects().stream()).toList();
        assertEquals(2, effects.size(), "Both actions must be present in the result");
        assertTrue(
                effects.stream()
                        .anyMatch(
                                ae ->
                                        "read".equals(ae.getAction())
                                                && ae.getEffect() == Effect.ALLOW),
                "READ must be ALLOW");
        assertTrue(
                effects.stream()
                        .anyMatch(
                                ae ->
                                        "write".equals(ae.getAction())
                                                && ae.getEffect() == Effect.ALLOW),
                "WRITE must be ALLOW");
    }

    @Test
    void getAllPermissions_multipleActions_partiallyCovered_allowedActionIsNotDropped() {
        // Only policy-backed rows are emitted. READ has a policy (ALLOW); WRITE has none so it
        // does not appear in the output — the caller receives only what the policy covers.
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        var readAction = buildResourceSetAction(1L, "read", resourceSet);
        // Only READ has a policy; WRITE has no policy for this scope
        AbacPolicyItem readPolicy = buildPolicyItem(1L, Effect.ALLOW, userSet, readAction, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read", "write"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of(readPolicy));

        when(abacPermissionMapper.toActionEffectDto(readPolicy))
                .thenReturn(buildActionEffect("user1", "us1", "read", Effect.ALLOW));
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        var effects =
                result.results().stream().flatMap(r -> r.getActionEffects().stream()).toList();
        assertEquals(1, effects.size(), "Only policy-backed READ must appear; WRITE has no policy");
        assertEquals(Effect.ALLOW, effects.get(0).getEffect());
        assertEquals("read", effects.get(0).getAction());
    }

    @Test
    void getAllPermissions_multiplePoliciesForSameResourceSet_groupsAndReturnsAllActions() {
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        var resourceAction1 = buildResourceSetAction(1L, "read", resourceSet);
        var resourceAction2 = buildResourceSetAction(2L, "write", resourceSet);
        AbacPolicyItem policyItem1 =
                buildPolicyItem(1L, Effect.ALLOW, userSet, resourceAction1, scope);
        AbacPolicyItem policyItem2 =
                buildPolicyItem(2L, Effect.DENY, userSet, resourceAction2, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read", "write"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of(policyItem1, policyItem2));

        when(abacPermissionMapper.toActionEffectDto(policyItem1))
                .thenReturn(buildActionEffect("user1", "us1", "read", Effect.ALLOW));
        when(abacPermissionMapper.toActionEffectDto(policyItem2))
                .thenReturn(buildActionEffect("user1", "us1", "write", Effect.DENY));
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        var effects =
                result.results().stream().flatMap(r -> r.getActionEffects().stream()).toList();
        assertTrue(
                effects.stream()
                        .anyMatch(
                                ae ->
                                        "read".equals(ae.getAction())
                                                && ae.getEffect() == Effect.ALLOW));
        assertTrue(
                effects.stream()
                        .anyMatch(
                                ae ->
                                        "write".equals(ae.getAction())
                                                && ae.getEffect() == Effect.DENY));
    }

    // ==================== User Set Hierarchy ====================

    @Test
    void getAllPermissions_initialQueryEmpty_findsParentUserSetPolicy() {
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet parentUserSet =
                buildUserSet(2L, "parent_us", "Parent UserSet", ConditionGroupOperator.AND);
        UserSet childUserSet =
                buildUserSetWithParents(
                        1L, "us1", "UserSet 1", ConditionGroupOperator.AND, List.of(parentUserSet));
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        var resourceAction = buildResourceSetAction(1L, "read", resourceSet);
        AbacPolicyItem policyItem =
                buildPolicyItem(1L, Effect.ALLOW, parentUserSet, resourceAction, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(childUserSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        // Initial query with child user set → empty
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), eq(List.of(childUserSet)), eq(project), any()))
                .thenReturn(List.of());
        // Hierarchy query with parent user set → returns policy
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), eq(List.of(parentUserSet)), eq(project), any()))
                .thenReturn(List.of(policyItem));

        when(abacPermissionMapper.toActionEffectDto(policyItem))
                .thenReturn(buildActionEffect("user1", "parent_us", "read", Effect.ALLOW));
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        // The hierarchy traversal finds a policy on the parent user set, but the post-traversal
        // filter discards it because parentUserSet was not attribute-evaluated (only childUserSet
        // was). The fallback therefore produces DENY.
        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY),
                "Parent user set policy is filtered out; result must be DENY");
    }

    @Test
    void getAllPermissions_initialQueryEmpty_userSetHasNoParents_returnsDeny() {
        Project project = buildProject(1L, "proj1", "Project 1");
        // User set with no parents — hierarchy search is skipped
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
    }

    @Test
    void getAllPermissions_userSetHierarchyReachesMaxDepthWithNoResult_returnsDeny() {
        // Triggers the max-depth path inside findPolicyItemsByUserSetHierarchy:
        // child → parent → grandparent, all return empty, depth limit reached.
        Project project = buildProject(1L, "proj1", "Project 1");
        UserSet grandparentUserSet =
                buildUserSet(
                        3L, "grandparent_us", "Grandparent UserSet", ConditionGroupOperator.AND);
        UserSet parentUserSet =
                buildUserSetWithParents(
                        2L,
                        "parent_us",
                        "Parent UserSet",
                        ConditionGroupOperator.AND,
                        List.of(grandparentUserSet));
        UserSet childUserSet =
                buildUserSetWithParents(
                        1L, "us1", "UserSet 1", ConditionGroupOperator.AND, List.of(parentUserSet));
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(childUserSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        // All queries return empty — max depth is reached with no policy found
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
    }

    @Test
    void getAllPermissions_userSetSizeMismatch_requeriesWithUpdatedUserSets() {
        // Initial query returns policy for only one of two queried user sets.
        // Service must re-query replacing the uncovered user set with its parent.
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet parentUserSet =
                buildUserSet(3L, "parent_us", "Parent UserSet", ConditionGroupOperator.AND);
        UserSet userSet1 =
                buildUserSetWithParents(
                        1L, "us1", "UserSet 1", ConditionGroupOperator.AND, List.of(parentUserSet));
        UserSet userSet2 = buildUserSet(2L, "us2", "UserSet 2", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        var resourceAction = buildResourceSetAction(1L, "read", resourceSet);
        // Policy only covers userSet2
        AbacPolicyItem policyItem =
                buildPolicyItem(1L, Effect.ALLOW, userSet2, resourceAction, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet1, userSet2));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        // Initial query: size mismatch (queried [us1, us2], policy only has [us2])
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), eq(List.of(userSet1, userSet2)), eq(project), any()))
                .thenReturn(List.of(policyItem));
        // Re-query after mismatch: [us2 (covered), parentUs (parent of us1)]
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), eq(List.of(userSet2, parentUserSet)), eq(project), any()))
                .thenReturn(List.of(policyItem));

        when(abacPermissionMapper.toActionEffectDto(policyItem))
                .thenReturn(buildActionEffect("user1", "us2", "read", Effect.ALLOW));
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertNotNull(result);
        assertFalse(result.results().isEmpty());
    }

    // ==================== Resource Set Hierarchy ====================

    @Test
    void getAllPermissions_initialQueryEmpty_findsParentResourceSetPolicy() {
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        // User set with NO parents so user-set hierarchy is skipped entirely
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet parentResourceSet =
                buildResourceSet(2L, "parent_rs", "Parent ResourceSet", ConditionGroupOperator.AND);
        ResourceSet childResourceSet =
                buildResourceSetWithParent(
                        1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND, parentResourceSet);
        var parentResourceAction = buildResourceSetAction(2L, "read", parentResourceSet);
        AbacPolicyItem policyItem =
                buildPolicyItem(1L, Effect.ALLOW, userSet, parentResourceAction, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(childResourceSet));
        // Initial query with child resource set → empty
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        eq(List.of(childResourceSet)), any(), any(), eq(project), any()))
                .thenReturn(List.of());
        // Hierarchy query with parent resource set → returns policy
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        eq(List.of(parentResourceSet)), any(), any(), eq(project), any()))
                .thenReturn(List.of(policyItem));

        when(abacPermissionMapper.toActionEffectDto(policyItem))
                .thenReturn(buildActionEffect("user1", "us1", "read", Effect.ALLOW));
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "parent_rs", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        // The hierarchy traversal finds a policy on the parent resource set, but the post-traversal
        // filter discards it because parentResourceSet was not attribute-evaluated (only
        // childResourceSet was). The fallback therefore produces DENY.
        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY),
                "Parent resource set policy is filtered out; result must be DENY");
    }

    @Test
    void getAllPermissions_initialQueryEmpty_resourceSetHasNoParents_returnsDeny() {
        Project project = buildProject(1L, "proj1", "Project 1");
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        // Resource set with no parent — hierarchy search is skipped
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
    }

    @Test
    void getAllPermissions_resourceSetHierarchyReachesMaxDepthWithNoResult_returnsDeny() {
        Project project = buildProject(1L, "proj1", "Project 1");
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet grandparentResourceSet =
                buildResourceSet(
                        3L, "grandparent_rs", "Grandparent RS", ConditionGroupOperator.AND);
        ResourceSet parentResourceSet =
                buildResourceSetWithParent(
                        2L,
                        "parent_rs",
                        "Parent RS",
                        ConditionGroupOperator.AND,
                        grandparentResourceSet);
        ResourceSet childResourceSet =
                buildResourceSetWithParent(
                        1L, "rs1", "RS 1", ConditionGroupOperator.AND, parentResourceSet);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(childResourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
    }

    @Test
    void getAllPermissions_resourceSetSizeMismatch_requeriesWithUpdatedResourceSets() {
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet parentResourceSet =
                buildResourceSet(3L, "parent_rs", "Parent ResourceSet", ConditionGroupOperator.AND);
        ResourceSet resourceSet1 =
                buildResourceSetWithParent(
                        1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND, parentResourceSet);
        ResourceSet resourceSet2 =
                buildResourceSet(2L, "rs2", "ResourceSet 2", ConditionGroupOperator.AND);
        var resourceAction2 = buildResourceSetAction(2L, "read", resourceSet2);
        // Policy only covers resourceSet2
        AbacPolicyItem policyItem =
                buildPolicyItem(1L, Effect.ALLOW, userSet, resourceAction2, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet1, resourceSet2));
        // Initial query: size mismatch (queried [rs1, rs2], policy only covers [rs2])
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        eq(List.of(resourceSet1, resourceSet2)), any(), any(), eq(project), any()))
                .thenReturn(List.of(policyItem));
        // Re-query after mismatch: [rs2 (covered), parentRs (parent of rs1)]
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        eq(List.of(resourceSet2, parentResourceSet)),
                        any(),
                        any(),
                        eq(project),
                        any()))
                .thenReturn(List.of(policyItem));

        when(abacPermissionMapper.toActionEffectDto(policyItem))
                .thenReturn(buildActionEffect("user1", "us1", "read", Effect.ALLOW));
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs2", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertNotNull(result);
        assertFalse(result.results().isEmpty());
    }

    // ==================== Condition Evaluation Filtering ====================

    @Test
    void getAllPermissions_userSetEvaluationReturnsFalse_userSetIsFilteredOut() {
        Project project = buildProject(1L, "proj1", "Project 1");
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);

        var user = buildUser("user1", "scope1", buildAttributes("role", "user"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        conditionEvaluationService =
                AbacPermissionMockFactory.createConditionEvaluationServiceWithResult(false);
        rebuildService();

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", null, null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        // User set was filtered out → no matching user sets → DENY
        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
    }

    @Test
    void getAllPermissions_resourceSetEvaluationReturnsFalse_resourceSetIsFilteredOut() {
        Project project = buildProject(1L, "proj1", "Project 1");
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "secret"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        var mockConditionEvalService = AbacPermissionMockFactory.createConditionEvaluationService();
        when(mockConditionEvalService.evaluateConditionalGroup(any(), any(), any()))
                .thenReturn(new EvaluationResult(true, null)) // user set → passes
                .thenReturn(new EvaluationResult(false, null)); // resource set → filtered out
        conditionEvaluationService = mockConditionEvalService;
        rebuildService();

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", null, null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        // Resource set was filtered out → no matching resource sets → DENY
        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
    }

    // ==================== DENY Response Builder Branches ====================

    @Test
    void getAllPermissions_noPolicyItems_bothUserAndResourceSets_returnsDenyPerResourceSet() {
        Project project = buildProject(1L, "proj1", "Project 1");
        UserSet userSet1 = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        UserSet userSet2 = buildUserSet(2L, "us2", "UserSet 2", ConditionGroupOperator.AND);
        ResourceSet resourceSet1 =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet2 =
                buildResourceSet(2L, "rs2", "ResourceSet 2", ConditionGroupOperator.AND);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet1, userSet2));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet1, resourceSet2));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        // One response per resource set (2), each containing one DENY per (userSet × action) = 2
        // buildPermissionDeniedResponses emits one DENY row per (resourceSet × action) with
        // userSet=null — user set keys are not included in the fallback DENY path.
        assertEquals(2, result.results().size(), "One response per resource set");
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
        result.results()
                .forEach(
                        r ->
                                assertEquals(
                                        1,
                                        r.getActionEffects().size(),
                                        "One DENY entry per action (userSet is null in fallback)"));
    }

    @Test
    void getAllPermissions_noPolicyItems_onlyUserSets_returnsDenyWithNullResourceSet() {
        Project project = buildProject(1L, "proj1", "Project 1");
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read", "write"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", null, null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
    }

    @Test
    void getAllPermissions_noPolicyItems_onlyResourceSets_returnsDenyPerResourceSet() {
        Project project = buildProject(1L, "proj1", "Project 1");
        ResourceSet resourceSet =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertFalse(result.results().isEmpty());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
    }

    @Test
    void getAllPermissions_noPolicyItems_noUserOrResourceSets_returnsSingleDenyResponse() {
        Project project = buildProject(1L, "proj1", "Project 1");

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());

        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", null, null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        assertEquals(1, result.results().size());
        assertTrue(
                result.results().stream()
                        .flatMap(r -> r.getActionEffects().stream())
                        .allMatch(ae -> ae.getEffect() == Effect.DENY));
    }

    // ==================== Multiple Resources ====================

    @Test
    void getAllPermissions_multipleResourcesInRequest_eachProcessedIndependently() {
        Project project = buildProject(1L, "proj1", "Project 1");
        AbacScope scope = buildScope(1L, "scope1", "Scope 1", project);
        UserSet userSet = buildUserSet(1L, "us1", "UserSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet1 =
                buildResourceSet(1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND);
        ResourceSet resourceSet2 =
                buildResourceSet(2L, "rs2", "ResourceSet 2", ConditionGroupOperator.AND);
        var action1 = buildResourceSetAction(1L, "read", resourceSet1);
        var action2 = buildResourceSetAction(2L, "read", resourceSet2);
        AbacPolicyItem policy1 = buildPolicyItem(1L, Effect.ALLOW, userSet, action1, scope);
        AbacPolicyItem policy2 = buildPolicyItem(2L, Effect.DENY, userSet, action2, scope);

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource1 = buildResource("res1", "scope1", buildAttributes("type", "doc"));
        var resource2 = buildResource("res2", "scope1", buildAttributes("type", "secret"));
        var request =
                buildPermissionRequest(
                        user,
                        List.of(
                                buildResourceActions(resource1, List.of("read")),
                                buildResourceActions(resource2, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet1));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of(policy1))
                .thenReturn(List.of(policy2));

        when(abacPermissionMapper.toActionEffectDto(policy1))
                .thenReturn(buildActionEffect("user1", "us1", "read", Effect.ALLOW));
        when(abacPermissionMapper.toActionEffectDto(policy2))
                .thenReturn(buildActionEffect("user1", "us1", "read", Effect.DENY));
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", "rs1", null));

        ListResponseDto<AbacPermissionResponseDto> result = service.getAllPermissions(request);

        // Two resources → at least two response groups
        assertFalse(result.results().isEmpty());
        assertEquals(2, result.results().size());
    }

    // ==================== Attribute Type Validation Tests ====================

    @Test
    void
            getAllPermissions_userSetCondition_numberAttributeIsQuotedString_throwsInvalidAttributeTypeException() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.NUMBER, "departmentID"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(OperandType.LITERAL, OperandDataType.NUMBER, "7"));
        var condition = buildUserSetCondition(ConditionOperator.EQUALS, left, right);
        UserSet userSet =
                buildUserSetWithConditions(
                        1L, "us1", "UserSet 1", ConditionGroupOperator.AND, List.of(condition));

        // "departmentID" sent as a quoted string instead of a native JSON number
        var user = buildUser("user1", "scope1", buildAttributes("departmentID", "7"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());

        assertThrows(
                AbacAttributeInvalidTypeException.class, () -> service.getAllPermissions(request));
    }

    @Test
    void
            getAllPermissions_userSetCondition_booleanAttributeIsQuotedString_throwsInvalidAttributeTypeException() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.BOOLEAN, "isActive"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(
                                OperandType.LITERAL, OperandDataType.BOOLEAN, "true"));
        var condition = buildUserSetCondition(ConditionOperator.EQUALS, left, right);
        UserSet userSet =
                buildUserSetWithConditions(
                        1L, "us1", "UserSet 1", ConditionGroupOperator.AND, List.of(condition));

        // "isActive" sent as a quoted string instead of a native JSON boolean
        var user = buildUser("user1", "scope1", buildAttributes("isActive", "true"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());

        assertThrows(
                AbacAttributeInvalidTypeException.class, () -> service.getAllPermissions(request));
    }

    @Test
    void
            getAllPermissions_resourceSetCondition_numberAttributeIsQuotedString_throwsInvalidAttributeTypeException() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var left =
                buildResourceSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.NUMBER, "price"));
        var right =
                buildResourceSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(OperandType.LITERAL, OperandDataType.NUMBER, "100"));
        var condition = buildResourceSetCondition(ConditionOperator.EQUALS, left, right);
        ResourceSet resourceSet =
                buildResourceSetWithConditions(
                        1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND, List.of(condition));

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        // "price" sent as a quoted string instead of a native JSON number
        var resource = buildResource("res1", "scope1", buildAttributes("price", "100"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));

        assertThrows(
                AbacAttributeInvalidTypeException.class, () -> service.getAllPermissions(request));
    }

    @Test
    void
            getAllPermissions_resourceSetCondition_booleanAttributeIsQuotedString_throwsInvalidAttributeTypeException() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var left =
                buildResourceSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.BOOLEAN, "isPublic"));
        var right =
                buildResourceSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(
                                OperandType.LITERAL, OperandDataType.BOOLEAN, "false"));
        var condition = buildResourceSetCondition(ConditionOperator.EQUALS, left, right);
        ResourceSet resourceSet =
                buildResourceSetWithConditions(
                        1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND, List.of(condition));

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        // "isPublic" sent as a quoted string instead of a native JSON boolean
        var resource = buildResource("res1", "scope1", buildAttributes("isPublic", "false"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));

        assertThrows(
                AbacAttributeInvalidTypeException.class, () -> service.getAllPermissions(request));
    }

    @Test
    void getAllPermissions_userSetCondition_numberAttributeIsNull_doesNotThrow() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.NUMBER, "departmentID"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(OperandType.LITERAL, OperandDataType.NUMBER, "7"));
        var condition = buildUserSetCondition(ConditionOperator.EQUALS, left, right);
        UserSet userSet =
                buildUserSetWithConditions(
                        1L, "us1", "UserSet 1", ConditionGroupOperator.AND, List.of(condition));

        // "departmentID" is absent — AbacPermissionServiceImpl.validateAttributeType skips null
        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", null, null));

        assertDoesNotThrow(() -> service.getAllPermissions(request));
    }

    @Test
    void getAllPermissions_userSetCondition_booleanAttributeIsNull_doesNotThrow() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.BOOLEAN, "isActive"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(
                                OperandType.LITERAL, OperandDataType.BOOLEAN, "true"));
        var condition = buildUserSetCondition(ConditionOperator.EQUALS, left, right);
        UserSet userSet =
                buildUserSetWithConditions(
                        1L, "us1", "UserSet 1", ConditionGroupOperator.AND, List.of(condition));

        // "isActive" is absent — validateAttributeType skips null values
        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(userSet));
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", null, null));

        assertDoesNotThrow(() -> service.getAllPermissions(request));
    }

    @Test
    void getAllPermissions_resourceSetCondition_numberAttributeIsNull_doesNotThrow() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var left =
                buildResourceSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.NUMBER, "price"));
        var right =
                buildResourceSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(OperandType.LITERAL, OperandDataType.NUMBER, "100"));
        var condition = buildResourceSetCondition(ConditionOperator.EQUALS, left, right);
        ResourceSet resourceSet =
                buildResourceSetWithConditions(
                        1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND, List.of(condition));

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        // "price" is absent — validateAttributeType skips null values
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", null, null));

        assertDoesNotThrow(() -> service.getAllPermissions(request));
    }

    @Test
    void getAllPermissions_resourceSetCondition_booleanAttributeIsNull_doesNotThrow() {
        Project project = buildProject(1L, "proj1", "Project 1");
        var left =
                buildResourceSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.BOOLEAN, "isPublic"));
        var right =
                buildResourceSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(
                                OperandType.LITERAL, OperandDataType.BOOLEAN, "false"));
        var condition = buildResourceSetCondition(ConditionOperator.EQUALS, left, right);
        ResourceSet resourceSet =
                buildResourceSetWithConditions(
                        1L, "rs1", "ResourceSet 1", ConditionGroupOperator.AND, List.of(condition));

        var user = buildUser("user1", "scope1", buildAttributes("role", "admin"));
        // "isPublic" is absent — validateAttributeType skips null values
        var resource = buildResource("res1", "scope1", buildAttributes("type", "document"));
        var request =
                buildPermissionRequest(
                        user, List.of(buildResourceActions(resource, List.of("read"))));

        when(authorRequestScope.getProject()).thenReturn(project);
        when(userSetRepository.findAllByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of());
        when(resourceSetRepository.findByAttributesAndProject(any(), eq(project)))
                .thenReturn(List.of(resourceSet));
        when(abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        any(), any(), any(), eq(project), any()))
                .thenReturn(List.of());
        when(abacPermissionMapper.toResourceResponseDto(any(), any()))
                .thenReturn(buildResourceResponse("res1", null, null));

        assertDoesNotThrow(() -> service.getAllPermissions(request));
    }
}
