package com.hmg.role.rbac.user.dto;

import com.hmg.role.rbac.userscoperole.dto.UpdateUserScopeRoleDto;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

public record UpdateUserDto(
        @Schema(title = "User name") String name,
        @Schema(title = "User metadata") Map<String, String> metadata,
        @Schema(title = "Scope role") @NoDuplicateValues List<UpdateUserScopeRoleDto> scopeRoles) {}
