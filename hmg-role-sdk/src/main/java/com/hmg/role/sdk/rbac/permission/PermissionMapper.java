package com.hmg.role.sdk.rbac.permission;

import com.hmg.role.sdk.rbac.permission.dto.*;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByRole;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByRoleDto;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByUser;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponse;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponseDto;
import com.hmg.role.sdk.rbac.permission.dto.PermissionResponseDto;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemKeyDto;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PermissionMapper {

    PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

    @Mapping(target = "effect", ignore = true)
    PermissionFlatResponseDto toPermissionFlatResponse(PermissionFlatRequestByRole flatReq);

    PolicyItemKeyDto toPolicyItemKey(PolicyItemModel policyItem);

    PolicyItemKeyDto toPolicyItemKey(PermissionFlatResponseDto flatResponseDto);

    @Mapping(target = "actionEffects", ignore = true)
    PermissionResponseDto toPermissionResponse(PermissionFlatResponse flatResponse);

    PermissionResponseDto.ActionEffectDto toActionEffect(PermissionFlatResponse flatResponse);

    @Mapping(target = "roleKey", ignore = true)
    PermissionFlatRequestByRoleDto toPermissionFlatRequestByRoleDto(
            PermissionFlatRequestByUser requestByUser);
}
