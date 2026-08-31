package com.hmg.role.rbac.permission.interfaces;

import com.hmg.role.rbac.permission.dto.PermissionFlattenedResponseDto;
import com.hmg.role.rbac.permission.dto.PermissionRequestDto;
import com.hmg.role.rbac.permission.dto.PermissionResponseDto;
import com.hmg.role.util.dto.ListResponseDto;

public interface PermissionService {

    ListResponseDto<PermissionResponseDto> getAllPermissions(PermissionRequestDto request);

    ListResponseDto<PermissionFlattenedResponseDto> getAllPermissionsFlattened(
            PermissionRequestDto request);
}
