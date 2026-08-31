package com.hmg.role.rbac.permission;

import static com.hmg.role.rbac.permission.dto.PermissionRequestDto.ResourceActionsDto.ResourceRequestDto;
import static com.hmg.role.rbac.permission.dto.PermissionResponseDto.ActionEffectDto;
import static com.hmg.role.rbac.permission.dto.PermissionResponseDto.ResourceResponseDto;

import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.permission.dto.PermissionFlattenedResponseDto;
import com.hmg.role.rbac.permission.dto.PermissionRequestDto;
import com.hmg.role.rbac.permission.dto.PermissionResponseDto;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class PermissionMapper {

    public Stream<PermissionFlattenedResponseDto> toPermissionFlattenedResponseDtoStream(
            PermissionResponseDto permissionResponseDto) {

        var resource = permissionResponseDto.getResource();
        return permissionResponseDto.getActionEffects().stream()
                .map(
                        actionEffectDto ->
                                toPermissionFlattenedResponseDto(actionEffectDto, resource));
    }

    protected abstract PermissionFlattenedResponseDto toPermissionFlattenedResponseDto(
            ActionEffectDto actionEffect, ResourceResponseDto resource);

    protected abstract ResourceResponseDto toResourceResponseDto(ResourceRequestDto resource);

    @Mapping(target = "action", source = "resourceAction.actionName")
    @Mapping(target = "role", source = "role.key")
    protected abstract ActionEffectDto toActionEffectDto(PolicyItem policyItem);

    public ResourceResponseDto toResourceResponseDto(
            PermissionRequestDto.ResourceActionsDto resourceRequest,
            String joinedScope,
            String resourceScope) {

        var resourceDto = new ResourceResponseDto();
        resourceDto.setType(resourceRequest.resource().type());

        if (StringUtils.isBlank(resourceScope) || StringUtils.isBlank(joinedScope)) {
            resourceDto.setScope(resourceScope);
        } else if (StringUtils.equals(resourceScope, joinedScope)) {
            resourceDto.setScope(joinedScope);
        } else {
            resourceDto.setScope(resourceScope);
        }

        return resourceDto;
    }

    @Mapping(source = "resourceResponseDto", target = "resource")
    @Mapping(source = "actionEffectDto", target = "actionEffects")
    public abstract PermissionResponseDto toPermissionResponseDto(
            ResourceResponseDto resourceResponseDto, List<ActionEffectDto> actionEffectDto);
}
