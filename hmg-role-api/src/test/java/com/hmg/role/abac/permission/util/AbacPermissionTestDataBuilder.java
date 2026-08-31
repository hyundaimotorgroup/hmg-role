package com.hmg.role.abac.permission.util;

import static com.hmg.role.abac.permission.dto.AbacPermissionRequestDto.AbacInstanceValuesDto;
import static com.hmg.role.abac.permission.dto.AbacPermissionRequestDto.AbacResourceActionsDto;

import com.hmg.role.abac.permission.dto.AbacPermissionRequestDto;
import com.hmg.role.abac.permission.dto.AbacPermissionResponseDto;
import com.hmg.role.abac.policy.AbacPolicyItem;
import com.hmg.role.abac.resourceset.ResourceSet;
import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.resourceset.condition.ResourceSetCondition;
import com.hmg.role.abac.resourceset.operand.ResourceSetOperand;
import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.abac.userset.UserSet;
import com.hmg.role.abac.userset.attributes.ConditionOperand;
import com.hmg.role.abac.userset.condition.UserSetCondition;
import com.hmg.role.abac.userset.condition.UserSetOperand;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.util.enums.ConditionGroupOperator;
import com.hmg.role.util.enums.ConditionOperator;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandPosition;
import com.hmg.role.util.enums.OperandType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Utility class for building test data for ABAC Permission tests. */
public class AbacPermissionTestDataBuilder {

    // ==================== AbacPermissionRequestDto Builders ====================

    public static AbacPermissionRequestDto buildPermissionRequest(
            AbacInstanceValuesDto user, List<AbacResourceActionsDto> resources) {
        return AbacPermissionRequestDto.builder().user(user).resources(resources).build();
    }

    public static AbacInstanceValuesDto buildUser(
            String id, String scope, Map<String, Object> attributes) {
        return AbacInstanceValuesDto.builder().scope(scope).attributes(attributes).build();
    }

    public static AbacResourceActionsDto buildResourceActions(
            AbacInstanceValuesDto resource, List<String> actions) {
        return AbacResourceActionsDto.builder().resource(resource).actions(actions).build();
    }

    public static AbacInstanceValuesDto buildResource(
            String id, String scope, Map<String, Object> attributes) {
        return AbacInstanceValuesDto.builder().scope(scope).attributes(attributes).build();
    }

    public static Map<String, Object> buildAttributes(String... keyValuePairs) {
        Map<String, Object> attributes = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            attributes.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return attributes;
    }

    public static Map<String, Object> buildAttributesWithNullValue(String key) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(key, null);
        return attributes;
    }

    // ==================== Domain Entity Builders ====================

    public static UserSet buildUserSet(
            Long id, String key, String name, ConditionGroupOperator conditionOperator) {
        UserSet userSet = new UserSet();
        userSet.setId(id);
        userSet.setKey(key);
        userSet.setName(name);
        userSet.setConditionGroupOperator(conditionOperator);
        userSet.setConditions(new ArrayList<>());
        userSet.setParents(new ArrayList<>());
        return userSet;
    }

    public static UserSet buildUserSetWithConditions(
            Long id,
            String key,
            String name,
            ConditionGroupOperator conditionOperator,
            List<UserSetCondition> conditions) {
        UserSet userSet = buildUserSet(id, key, name, conditionOperator);
        userSet.setConditions(conditions);
        return userSet;
    }

    public static UserSet buildUserSetWithParents(
            Long id,
            String key,
            String name,
            ConditionGroupOperator conditionOperator,
            List<UserSet> parents) {
        UserSet userSet = buildUserSet(id, key, name, conditionOperator);
        userSet.setParents(parents);
        return userSet;
    }

    public static ResourceSet buildResourceSet(
            Long id, String key, String name, ConditionGroupOperator conditionOperator) {
        ResourceSet resourceSet = new ResourceSet();
        resourceSet.setId(id);
        resourceSet.setKey(key);
        resourceSet.setName(name);
        resourceSet.setConditionGroupOperator(conditionOperator);
        resourceSet.setConditionGroup(new ArrayList<>());
        resourceSet.setParent(null); // Explicitly set parent to null
        return resourceSet;
    }

    public static ResourceSet buildResourceSetWithConditions(
            Long id,
            String key,
            String name,
            ConditionGroupOperator conditionOperator,
            List<ResourceSetCondition> conditions) {
        ResourceSet resourceSet = buildResourceSet(id, key, name, conditionOperator);
        resourceSet.setConditionGroup(conditions);
        return resourceSet;
    }

    public static ResourceSet buildResourceSetWithParent(
            Long id,
            String key,
            String name,
            ConditionGroupOperator conditionOperator,
            ResourceSet parent) {
        ResourceSet resourceSet = buildResourceSet(id, key, name, conditionOperator);
        resourceSet.setParent(parent);
        return resourceSet;
    }

    public static AbacPolicyItem buildPolicyItem(
            Long id,
            Effect effect,
            UserSet userSet,
            ResourceSetAction resourceSetAction,
            AbacScope scope) {
        AbacPolicyItem policyItem = new AbacPolicyItem();
        policyItem.setId(id);
        policyItem.setEffect(effect);
        policyItem.setUserSet(userSet);
        policyItem.setResourceSetAction(resourceSetAction);
        policyItem.setScope(scope);
        policyItem.setDeleted(false);
        return policyItem;
    }

    public static ResourceSetAction buildResourceSetAction(
            Long id, String actionName, ResourceSet resourceSet) {
        ResourceSetAction action = new ResourceSetAction();
        action.setId(id);
        action.setActionName(actionName);
        action.setResourceSet(resourceSet);
        return action;
    }

    public static Project buildProject(Long id, String key, String name) {
        Project project = new Project();
        project.setId(id);
        project.setKey(key);
        project.setName(name);
        return project;
    }

    public static AbacScope buildScope(Long id, String key, String name, Project project) {
        AbacScope scope = new AbacScope();
        scope.setId(id);
        scope.setKey(key);
        scope.setName(name);
        scope.setProject(project);
        scope.setDeleted(false);
        return scope;
    }

    // ==================== Response DTO Builders ====================

    public static AbacPermissionResponseDto.ActionEffectDto buildActionEffect(
            String userId, String userSet, String action, Effect effect) {
        return AbacPermissionResponseDto.ActionEffectDto.builder()
                .userSet(userSet)
                .action(action)
                .effect(effect)
                .build();
    }

    public static AbacPermissionResponseDto.ResourceResponseDto buildResourceResponse(
            String id, String resourceSet, String scope) {
        AbacPermissionResponseDto.ResourceResponseDto resource =
                new AbacPermissionResponseDto.ResourceResponseDto();
        resource.setResourceSet(resourceSet);
        resource.setScope(Optional.ofNullable(scope).orElse("default_scope"));
        return resource;
    }

    // ==================== Condition & Operand Builders ====================

    public static ConditionOperand buildConditionOperand(
            OperandType type, OperandDataType dataType, String operand) {
        ConditionOperand co = new ConditionOperand();
        co.setType(type);
        co.setDataType(dataType);
        co.setOperand(operand);
        return co;
    }

    public static UserSetOperand buildUserSetOperand(
            OperandPosition position, ConditionOperand conditionOperand) {
        UserSetOperand operand = new UserSetOperand();
        operand.setPosition(position);
        operand.setConditionOperand(conditionOperand);
        return operand;
    }

    public static UserSetCondition buildUserSetCondition(
            ConditionOperator operator, UserSetOperand left, UserSetOperand right) {
        UserSetCondition condition = new UserSetCondition();
        condition.setOperator(operator);
        condition.setOperands(List.of(left, right));
        return condition;
    }

    public static ResourceSetOperand buildResourceSetOperand(
            OperandPosition position, ConditionOperand conditionOperand) {
        ResourceSetOperand operand = new ResourceSetOperand();
        operand.setPosition(position);
        operand.setConditionOperand(conditionOperand);
        return operand;
    }

    public static ResourceSetCondition buildResourceSetCondition(
            ConditionOperator operator, ResourceSetOperand left, ResourceSetOperand right) {
        ResourceSetCondition condition = new ResourceSetCondition();
        condition.setOperator(operator);
        condition.setOperands(List.of(left, right));
        return condition;
    }

    public static AbacPermissionResponseDto buildPermissionResponse(
            AbacPermissionResponseDto.ResourceResponseDto resource,
            List<AbacPermissionResponseDto.ActionEffectDto> actionEffects) {
        return AbacPermissionResponseDto.builder()
                .resource(resource)
                .actionEffects(actionEffects)
                .build();
    }
}
