package com.hmg.role.rbac.policy;

import com.hmg.role.rbac.policy.dto.ActionWithEffectDto;
import com.hmg.role.rbac.policy.dto.PolicyDto;
import com.hmg.role.rbac.policy.dto.ResourceTypeWithPolicyActionsDto;
import com.hmg.role.rbac.resourceaction.ResourceAction;
import com.hmg.role.rbac.resourcetype.ResourceType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceTypePolicyMapper {

    public static List<ResourceTypeWithPolicyActionsDto> map(
            List<ResourceType> resourceTypes, List<PolicyDto> policies) {

        List<PolicyDto> allowPolicies =
                policies.stream().filter(p -> "ALLOW".equalsIgnoreCase(p.effect())).toList();

        // Map children berdasarkan parent key
        Map<String, List<ResourceType>> childrenMap =
                resourceTypes.stream()
                        .filter(rt -> rt.getParent() != null)
                        .collect(Collectors.groupingBy(rt -> rt.getParent().getKey()));

        // Ambil hanya root-level (tanpa parent)
        return resourceTypes.stream()
                .filter(rt -> rt.getParent() == null)
                .map(rt -> mapOne(rt, allowPolicies, childrenMap))
                .toList();
    }

    private static ResourceTypeWithPolicyActionsDto mapOne(
            ResourceType resourceType,
            List<PolicyDto> allowPolicies,
            Map<String, List<ResourceType>> childrenMap) {
        ResourceTypeWithPolicyActionsDto dto = new ResourceTypeWithPolicyActionsDto();
        dto.setKey(resourceType.getKey());
        dto.setName(resourceType.getName());

        List<ResourceAction> resourceActions = resourceType.getResourceActions();
        if (resourceActions == null) resourceActions = List.of();

        List<String> allowedActions =
                allowPolicies.stream()
                        .filter(p -> p.resourceType().equals(resourceType.getKey()))
                        .flatMap(p -> p.actions().stream())
                        .distinct()
                        .toList();

        List<ActionWithEffectDto> actionDtos =
                resourceActions.stream()
                        .map(
                                ra -> {
                                    ActionWithEffectDto actionDto = new ActionWithEffectDto();
                                    actionDto.setAction(ra.getActionName());
                                    actionDto.setAllowed(
                                            allowedActions.contains(ra.getActionName()));
                                    return actionDto;
                                })
                        .toList();
        dto.setActions(actionDtos);

        List<ResourceType> children = childrenMap.getOrDefault(resourceType.getKey(), List.of());
        List<ResourceTypeWithPolicyActionsDto> childDtos =
                children.stream().map(child -> mapOne(child, allowPolicies, childrenMap)).toList();
        dto.setChildren(childDtos);

        return dto;
    }
}
