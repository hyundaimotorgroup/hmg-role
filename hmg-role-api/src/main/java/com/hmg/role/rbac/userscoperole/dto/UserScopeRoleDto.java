package com.hmg.role.rbac.userscoperole.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserScopeRoleDto(
        @Schema(title = "role key") String roleKey,
        @Schema(title = "role name") String roleName,
        @Schema(title = "scope key") String scopeKey,
        @Schema(title = "scope key") String scopeName) {}
