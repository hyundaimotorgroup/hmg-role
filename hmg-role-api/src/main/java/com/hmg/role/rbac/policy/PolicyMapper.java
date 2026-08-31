package com.hmg.role.rbac.policy;

import com.hmg.role.admin.project.Project;
import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.policy.dto.CreatePolicyDto;
import com.hmg.role.rbac.policy.dto.PolicyDto;
import com.hmg.role.rbac.policy.dto.UpdateBulkPolicyDto;
import com.hmg.role.rbac.policy.dto.UpdatePolicyDto;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public interface PolicyMapper {

    Policy toPolicy(CreatePolicyDto dto);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "key")
    @Mapping(source = "updatePolicyDto.description", target = "description")
    @Mapping(source = "project", target = "project")
    void toPolicy(
            @MappingTarget Policy existingPolicy, UpdatePolicyDto updatePolicyDto, Project project);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "key")
    @Mapping(source = "updateBulkPolicyDto.description", target = "description")
    @Mapping(source = "project", target = "project")
    void toPolicy(
            @MappingTarget Policy existingPolicy,
            UpdateBulkPolicyDto updateBulkPolicyDto,
            Project project);

    default PolicyDto toPolicyDto(
            PolicyItem policyItem, List<String> resourceActions, List<String> roles) {
        return PolicyDto.builder()
                .key(policyItem.getPolicy().getKey())
                .description(policyItem.getPolicy().getDescription())
                .scopeKey(policyItem.getScope().getKey())
                .resourceType(policyItem.getResourceAction().getResourceType().getKey())
                .actions(resourceActions)
                .roles(roles)
                .effect(policyItem.getEffect().toString())
                .build();
    }

    @Mapping(source = "scope", target = "scopeKey")
    PolicyDto toPolicyDto(CreatePolicyDto req, String scope);

    @Mapping(source = "scope", target = "scopeKey")
    PolicyDto toPolicyDto(UpdateBulkPolicyDto req, String scope);

    UpdateBulkPolicyDto toUpdateBulkPolicyDto(String key, UpdatePolicyDto dto);
}
