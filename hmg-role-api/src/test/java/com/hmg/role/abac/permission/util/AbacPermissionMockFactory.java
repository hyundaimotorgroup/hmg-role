package com.hmg.role.abac.permission.util;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hmg.role.abac.logicalexpression.ConditionEvaluationService;
import com.hmg.role.abac.logicalexpression.ConditionalExpression;
import com.hmg.role.abac.logicalexpression.EvaluationResult;
import com.hmg.role.abac.logicalexpression.LogicalExpressionMapper;
import com.hmg.role.abac.logicalexpression.interfaces.LogicalExpressionEvaluator;
import com.hmg.role.abac.permission.AbacPermissionMapper;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemRepository;
import com.hmg.role.abac.resourceset.ResourceSetRepository;
import com.hmg.role.abac.userset.UserSetRepository;
import com.hmg.role.admin.member.Member;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.scope.interfaces.ScopeService;
import com.hmg.role.util.AuthorRequestScope;

/**
 * Factory class for creating mock objects for ABAC Permission tests. Provides consistent mock
 * configurations across test cases.
 */
public class AbacPermissionMockFactory {

    public static UserSetRepository createUserSetRepository() {
        return mock(UserSetRepository.class);
    }

    public static ResourceSetRepository createResourceSetRepository() {
        return mock(ResourceSetRepository.class);
    }

    public static AbacPolicyItemRepository createAbacPolicyItemRepository() {
        return mock(AbacPolicyItemRepository.class);
    }

    public static AbacPermissionMapper createAbacPermissionMapper() {
        return mock(AbacPermissionMapper.class);
    }

    public static ConditionEvaluationService createConditionEvaluationService() {
        return mock(ConditionEvaluationService.class);
    }

    public static LogicalExpressionEvaluator createLogicalExpressionEvaluator() {
        return mock(LogicalExpressionEvaluator.class);
    }

    public static LogicalExpressionMapper createLogicalExpressionMapper() {
        LogicalExpressionMapper mapper = mock(LogicalExpressionMapper.class);
        when(mapper.toConditionalExpression(any(), any(), any()))
                .thenReturn(new ConditionalExpression());
        return mapper;
    }

    public static AuthorRequestScope createAuthorRequestScope() {
        return mock(AuthorRequestScope.class);
    }

    public static AuthorRequestScope createAuthorRequestScopeWithProject(Project project) {
        AuthorRequestScope scope = mock(AuthorRequestScope.class);
        when(scope.getProject()).thenReturn(project);
        return scope;
    }

    public static AuthorRequestScope createAuthorRequestScopeWithMember(Member member) {
        AuthorRequestScope scope = mock(AuthorRequestScope.class);
        when(scope.getProject()).thenReturn(null);
        when(scope.getMember()).thenReturn(member);
        return scope;
    }

    public static Member createMember(Project project) {
        Member member = mock(Member.class);
        when(member.getProject()).thenReturn(project);
        return member;
    }

    /**
     * Creates a ConditionEvaluationService that returns the specified result for all evaluations.
     */
    public static ConditionEvaluationService createConditionEvaluationServiceWithResult(
            boolean result) {
        ConditionEvaluationService service = mock(ConditionEvaluationService.class);
        EvaluationResult evaluationResult = new EvaluationResult(result, null);
        when(service.evaluateConditionalGroup(any(), any(), any())).thenReturn(evaluationResult);
        return service;
    }

    /** Creates a ScopeService that returns the specified exists result for scope checking. */
    public static ScopeService createScopeServiceWithExistsResult(boolean exists) {
        ScopeService service = mock(ScopeService.class);
        when(service.existsScopeKey(anyString(), any(Project.class))).thenReturn(exists);
        return service;
    }
}
