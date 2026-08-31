package com.hmg.role.abac.permission;

import static com.hmg.role.abac.permission.dto.AbacPermissionRequestDto.AbacInstanceValuesDto;
import static com.hmg.role.abac.permission.dto.AbacPermissionResponseDto.ActionEffectDto;
import static com.hmg.role.abac.permission.dto.AbacPermissionResponseDto.ResourceResponseDto;

import com.hmg.role.abac.permission.dto.AbacPermissionFlattenedResponseDto;
import com.hmg.role.abac.permission.dto.AbacPermissionResponseDto;
import com.hmg.role.abac.policy.AbacPolicyItem;
import com.hmg.role.common.config.CommonMapperConfig;
import java.util.stream.Stream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class AbacPermissionMapper {

    public Stream<AbacPermissionFlattenedResponseDto> toPermissionFlattenedResponseDtoStream(
            AbacPermissionResponseDto permissionResponseDto) {
        var resource = permissionResponseDto.getResource();
        return permissionResponseDto.getActionEffects().stream()
                .map(
                        actionEffectDto ->
                                toPermissionFlattenedResponseDto(actionEffectDto, resource));
    }

    protected abstract AbacPermissionFlattenedResponseDto toPermissionFlattenedResponseDto(
            ActionEffectDto actionEffect, ResourceResponseDto resource);

    protected abstract ResourceResponseDto toResourceResponseDto(
            AbacInstanceValuesDto resource, String resourceSet);

    @Mapping(target = "action", source = "policyItem.resourceSetAction.actionName")
    @Mapping(target = "userSet", source = "policyItem.userSet.key")
    protected abstract ActionEffectDto toActionEffectDto(AbacPolicyItem policyItem);
}
