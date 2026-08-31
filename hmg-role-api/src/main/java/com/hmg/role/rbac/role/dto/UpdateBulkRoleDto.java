package com.hmg.role.rbac.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateBulkRoleDto(
        @NotBlank @Schema(title = "key") String key,
        @NotBlank @Schema(title = "name") String name,
        @Schema(title = "description") String description) {}
