package com.hmg.role.sdk.rbac.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hmg.role.sdk.common.enums.Effect;
import com.hmg.role.sdk.common.exception.NotFoundException;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByRoleDto;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponse;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponseDto;
import com.hmg.role.sdk.rbac.permission.exception.ActionNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.PolicyNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.ResourceTypeNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.RoleNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.ScopeNotFoundException;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionSetModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.spi.PolicyItemProvider;
import com.hmg.role.sdk.rbac.permission.spi.ResourceActionProvider;
import com.hmg.role.sdk.rbac.permission.spi.RoleProvider;
import com.hmg.role.sdk.rbac.permission.spi.ScopeProvider;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@Tags({@Tag("small"), @Tag("unit"), @Tag("api")})
class PermissionServiceImplTest {

    private AutoCloseable mockHandle;

    @Mock private PolicyItemProvider policyItemProvider;
    @Mock private RoleProvider roleProvider;
    @Mock private ScopeProvider scopeProvider;
    @Mock private ResourceActionProvider resourceActionProvider;
    @Mock private DataExistenceValidator dataExistenceValidator;

    private PermissionService subject;

    @BeforeEach
    void setUp() {
        mockHandle = MockitoAnnotations.openMocks(this);
        subject =
                new PermissionServiceImpl(
                        policyItemProvider,
                        roleProvider,
                        scopeProvider,
                        resourceActionProvider,
                        dataExistenceValidator);
    }

    @AfterEach
    void tearDown() throws Exception {
        Mockito.reset(
                policyItemProvider,
                roleProvider,
                scopeProvider,
                resourceActionProvider,
                dataExistenceValidator);
        mockHandle.close();
    }

    // --- Tests ------------------------------------------------------------------

    @Test
    void getPermissionsFlattened_byRole_explicitAction_allow() throws NotFoundException {
        // Given: a role request with blank scope (should default), explicit action "read"
        PermissionFlatRequestByRoleDto req = baseRoleRequest("roleA", "", "doc", "doc-123", "read");

        // Scope defaulting for blank scope
        when(scopeProvider.getDefaultScopeRbac()).thenReturn(new MockScopeModel("default"));

        // No wildcard -> resourceActionProvider not consulted; provide a matching policy
        when(policyItemProvider.findAllPoliciesByKeys(anyCollection()))
                .thenAnswer(
                        inv -> {
                            // The service will supply a collection of PolicyItemKeyDto
                            Collection<PolicyItemModel> keys = inv.getArgument(0);
                            // Return stream with exactly matching policy
                            return Stream.of(
                                    new MockPolicyItemModel(
                                            "roleA",
                                            "default",
                                            "doc",
                                            "read",
                                            "doc-123",
                                            Effect.ALLOW));
                        });

        // When
        Collection<? extends PermissionFlatResponse> out = subject.getPermissionsFlattened(req);

        // Then
        assertNotNull(out);
        assertEquals(1, out.size());

        PermissionFlatResponseDto resp = (PermissionFlatResponseDto) out.iterator().next();
        assertEquals("roleA", resp.getRoleKey());
        assertEquals("default", resp.getScopeKey());
        assertEquals("doc", resp.getResourceTypeKey());
        assertEquals("read", resp.getActionName());
        assertEquals("doc-123", resp.getResourceId());
        assertEquals(Effect.ALLOW, resp.getEffect());

        // Ensure validators not called (default strategy is RETURN_PERMISSION_DENY)
        verifyNoInteractions(dataExistenceValidator);
        verify(scopeProvider, times(1)).getDefaultScopeRbac();
        verify(policyItemProvider, times(1)).findAllPoliciesByKeys(anyCollection());
    }

    @Test
    void getPermissionsFlattened_byRole_wildcard_expands_and_missing_policies_set_to_deny()
            throws NotFoundException {
        // Given: wildcard action "*", 2 actions exist: read & write
        PermissionFlatRequestByRoleDto req =
                baseRoleRequest("roleB", "team", "doc", "doc-999", "*");

        // Wildcard expansion
        when(resourceActionProvider.findActionsByType(any()))
                .thenReturn(
                        Stream.of(
                                new MockResourceActionModel("read"),
                                new MockResourceActionModel("write")));

        // Policies: only one present (read -> ALLOW); write missing -> DENY by default strategy
        when(policyItemProvider.findAllPoliciesByKeys(anyCollection()))
                .thenReturn(
                        Stream.of(
                                new MockPolicyItemModel(
                                        "roleB", "team", "doc", "read", "doc-999", Effect.ALLOW)));

        // When
        Collection<? extends PermissionFlatResponse> out = subject.getPermissionsFlattened(req);

        // Then
        assertNotNull(out);
        assertEquals(2, out.size());

        Map<String, Effect> effectByAction = new HashMap<>();
        for (PermissionFlatResponse r : out) {
            PermissionFlatResponseDto d = (PermissionFlatResponseDto) r;
            effectByAction.put(d.getActionName(), d.getEffect());
        }
        assertEquals(Effect.ALLOW, effectByAction.get("read"));
        assertEquals(
                Effect.DENY,
                effectByAction.get("write")); // default strategy sets DENY when policy missing

        verify(resourceActionProvider, times(1)).findActionsByType(any(ResourceTypeModel.class));
        verify(policyItemProvider, times(1)).findAllPoliciesByKeys(anyCollection());
        verifyNoInteractions(dataExistenceValidator);
    }

    @Test
    void getPermissionsFlattened_byRole_throwException_when_policy_missing_and_strategy_throw()
            throws RoleNotFoundException,
                    ScopeNotFoundException,
                    ActionNotFoundException,
                    ResourceTypeNotFoundException {
        // Given: same request, but service is built with THROW_EXCEPTION strategy
        PermissionServiceImpl throwingSubject =
                PermissionServiceImpl.builder()
                        .policyItemProvider(policyItemProvider)
                        .roleProvider(roleProvider)
                        .scopeProvider(scopeProvider)
                        .resourceActionProvider(resourceActionProvider)
                        .dataExistenceValidator(dataExistenceValidator)
                        .dataNotFoundStrategy(DataNotFoundStrategy.THROW_EXCEPTION)
                        .build();

        PermissionFlatRequestByRoleDto req =
                baseRoleRequest("roleC", "ops", "ticket", "T-1", "read");

        // No policy found for the requested key -> expect PolicyNotFoundException
        when(policyItemProvider.findAllPoliciesByKeys(anyCollection())).thenReturn(Stream.empty());

        // When / Then
        assertThrows(
                PolicyNotFoundException.class, () -> throwingSubject.getPermissionsFlattened(req));

        // Verify validators were called
        verify(dataExistenceValidator, times(1)).validateRole(any(RoleModel.class));
        verify(dataExistenceValidator, times(1)).validateScope(any(ScopeModel.class));
        verify(dataExistenceValidator, times(1))
                .validateResource(any(ResourceTypeModel.class), any(ResourceActionSetModel.class));
        verify(policyItemProvider, times(1)).findAllPoliciesByKeys(anyCollection());
    }

    // --- Helpers ----------------------------------------------------------------
    private PermissionFlatRequestByRoleDto baseRoleRequest(
            String roleKey,
            String scopeKey,
            String resourceTypeKey,
            String resourceId,
            String actionName) {
        PermissionFlatRequestByRoleDto req = new PermissionFlatRequestByRoleDto();
        req.setRoleKey(roleKey);
        req.setScopeKey(scopeKey);
        req.setResourceTypeKey(resourceTypeKey);
        req.setResourceId(resourceId);
        req.setActionName(actionName);
        return req;
    }

    private static class MockPolicyItemModel implements PolicyItemModel {
        String roleKey;
        String scopeKey;
        String resourceTypeKey;
        String actionName;
        String resourceId;
        Effect effect;

        public MockPolicyItemModel(
                String roleKey,
                String scopeKey,
                String resourceTypeKey,
                String actionName,
                String resourceId,
                Effect effect) {
            this.roleKey = roleKey;
            this.scopeKey = scopeKey;
            this.resourceTypeKey = resourceTypeKey;
            this.actionName = actionName;
            this.resourceId = resourceId;
            this.effect = effect;
        }

        @Override
        public String getRoleKey() {
            return roleKey;
        }

        @Override
        public String getScopeKey() {
            return scopeKey;
        }

        @Override
        public String getResourceTypeKey() {
            return resourceTypeKey;
        }

        @Override
        public String getActionName() {
            return actionName;
        }

        public String getResourceId() {
            return resourceId;
        }

        @Override
        public Effect getEffect() {
            return effect;
        }
    }

    private static class MockResourceTypeModel implements ResourceTypeModel {
        String resourceTypeKey;

        public MockResourceTypeModel(String resourceTypeKey) {
            this.resourceTypeKey = resourceTypeKey;
        }

        @Override
        public String getResourceTypeKey() {
            return resourceTypeKey;
        }
    }

    private static class MockScopeModel implements ScopeModel {
        String scopeKey;

        public MockScopeModel(String scopeKey) {
            this.scopeKey = scopeKey;
        }

        @Override
        public String getScopeKey() {
            return scopeKey;
        }
    }

    private static class MockResourceActionModel implements ResourceActionModel {
        String actionName;

        public MockResourceActionModel(String actionName) {
            this.actionName = actionName;
        }

        @Override
        public String getActionName() {
            return actionName;
        }
    }
}
