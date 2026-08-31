package com.hmg.role.abac.policy;

import com.hmg.role.abac.policy.dto.AbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateBulkAbacPolicyDto;
import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.userset.UserSet;
import com.hmg.role.admin.project.Project;
import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.policy.enums.Effect;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public abstract class AbacPolicyMapper {

    @Mapping(ignore = true, target = "id")
    @Mapping(source = "abacPolicyDto.key", target = "key")
    @Mapping(source = "abacPolicyDto.description", target = "description")
    @Mapping(source = "project", target = "project")
    @Mapping(ignore = true, target = "deleted")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(ignore = true, target = "createdAt")
    @Mapping(source = "updatedBy", target = "updatedBy")
    @Mapping(ignore = true, target = "updatedAt")
    public abstract AbacPolicy toPolicy(
            AbacPolicyDto abacPolicyDto, Project project, String createdBy, String updatedBy);

    @Mapping(source = "updateAbacPolicyDto.description", target = "description")
    @Mapping(source = "updatedBy", target = "updatedBy")
    @Mapping(ignore = true, target = "updatedAt")
    public abstract void toPolicy(
            @MappingTarget AbacPolicy policy,
            UpdateAbacPolicyDto updateAbacPolicyDto,
            String updatedBy);

    @Mapping(ignore = true, target = "key")
    @Mapping(source = "updateBulkAbacPolicyDto.description", target = "description")
    @Mapping(source = "updatedBy", target = "updatedBy")
    @Mapping(ignore = true, target = "updatedAt")
    public abstract void toPolicy(
            @MappingTarget AbacPolicy policy,
            UpdateBulkAbacPolicyDto updateBulkAbacPolicyDto,
            String updatedBy);

    public AbacPolicyDto toPolicyDto(List<AbacPolicyItem> policyItems) {
        var resourceSet = policyItems.getFirst().getResourceSetAction().getResourceSet();

        var userSet =
                policyItems.stream()
                        .map(AbacPolicyItem::getUserSet)
                        .map(UserSet::getKey)
                        .distinct()
                        .toList();
        var actions =
                policyItems.stream()
                        .map(AbacPolicyItem::getResourceSetAction)
                        .map(ResourceSetAction::getActionName)
                        .distinct()
                        .toList();

        var effect =
                policyItems.stream().map(AbacPolicyItem::getEffect).findAny().orElse(Effect.DENY);

        var policy =
                policyItems.stream()
                        .map(AbacPolicyItem::getPolicy)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElseThrow();

        var scope = policyItems.stream().map(k -> k.getScope().getKey()).findFirst().orElseThrow();

        return AbacPolicyDto.builder()
                .key(policy.getKey())
                .description(policy.getDescription())
                .scope(scope)
                .userSets(userSet)
                .actions(actions)
                .resourceSet(resourceSet.getKey())
                .effect(effect)
                .build();
    }

    // This method used for return Update API response
    public AbacPolicyDto toPolicyDto(AbacPolicy policy, List<AbacPolicyItem> policyItems) {
        var resourceSet = policyItems.getFirst().getResourceSetAction().getResourceSet();

        var userSet =
                policyItems.stream()
                        .map(AbacPolicyItem::getUserSet)
                        .map(UserSet::getKey)
                        .distinct()
                        .toList();
        var actions =
                policyItems.stream()
                        .map(AbacPolicyItem::getResourceSetAction)
                        .map(ResourceSetAction::getActionName)
                        .distinct()
                        .toList();

        var effect =
                policyItems.stream().map(AbacPolicyItem::getEffect).findAny().orElse(Effect.DENY);

        var scope = policyItems.stream().map(k -> k.getScope().getKey()).findFirst().orElseThrow();

        return AbacPolicyDto.builder()
                .key(policy.getKey())
                .description(policy.getDescription())
                .scope(scope)
                .userSets(userSet)
                .actions(actions)
                .resourceSet(resourceSet.getKey())
                .effect(effect)
                .build();
    }

    public List<AbacPolicyDto> ungroupPolicyToPolicyDto(List<AbacPolicyItem> policyItems) {
        var policies = policyItems.stream().map(AbacPolicyItem::getPolicy).distinct().toList();

        List<AbacPolicyDto> dtoList = new ArrayList<>();

        policies.forEach(
                policy -> {
                    var policyItemsByPolicy =
                            policyItems.stream()
                                    .filter(item -> item.getPolicy().equals(policy))
                                    .toList();

                    var dto = this.toPolicyDto(policyItemsByPolicy);

                    dtoList.add(dto);
                });

        return dtoList;
    }
}
