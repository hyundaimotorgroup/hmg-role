package com.hmg.role.rbac.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateRoleDto(
        @Schema(title = "name") String name, @Schema(title = "description") String description) {}
