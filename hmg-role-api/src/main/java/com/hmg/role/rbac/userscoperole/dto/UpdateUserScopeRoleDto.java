package com.hmg.role.rbac.userscoperole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserScopeRoleDto(
        @NotBlank @Schema(title = "role key") String roleKey,
        @NotBlank @Schema(title = "scope key") String scopeKey) {}
