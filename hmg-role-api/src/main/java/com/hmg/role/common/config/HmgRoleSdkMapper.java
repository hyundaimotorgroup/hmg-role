package com.hmg.role.common.config;

import com.hmg.role.rbac.permission.dto.PermissionFlattenedResponseDto;
import com.hmg.role.rbac.permission.dto.PermissionRequestDto;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponse;
import com.hmg.role.sdk.rbac.permission.dto.PermissionResponse;
import com.hmg.role.sdk.rbac.permission.dto.ResourceRequestDto;
import com.hmg.role.sdk.rbac.permission.dto.UserSubjectRequest;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemModel;

public interface HmgRoleSdkMapper {

    PermissionFlattenedResponseDto permissionFlattenedResponseDto(PermissionFlatResponse sdkResp);

    com.hmg.role.rbac.permission.dto.PermissionResponseDto toPermissionResponseDto(
            PermissionResponse sdkResp);

    UserSubjectRequest toSdkUserSubjectRequest(
            PermissionRequestDto.PermissionRequestUserDto apiUserDto);

    ResourceRequestDto toSdkResourceRequestDto(PermissionRequestDto.ResourceActionsDto apiReq);

    PolicyItemModel toSdkPolicyItemModel(PolicyItem pi);
}
