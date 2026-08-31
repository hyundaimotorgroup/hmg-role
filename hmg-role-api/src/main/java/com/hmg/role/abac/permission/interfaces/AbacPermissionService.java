package com.hmg.role.abac.permission.interfaces;

import com.hmg.role.abac.permission.dto.AbacPermissionFlattenedResponseDto;
import com.hmg.role.abac.permission.dto.AbacPermissionRequestDto;
import com.hmg.role.abac.permission.dto.AbacPermissionResponseDto;
import com.hmg.role.util.dto.ListResponseDto;

public interface AbacPermissionService {
    ListResponseDto<AbacPermissionResponseDto> getAllPermissions(AbacPermissionRequestDto request);

    ListResponseDto<AbacPermissionFlattenedResponseDto> getAllPermissionsFlattened(
            AbacPermissionRequestDto request);
}
