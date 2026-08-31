package com.hmg.role.rbac.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleDto(
        @Schema(title = "role name") @NotBlank @Size(max = 50) String name,
        @Schema(title = "role key") @NotBlank @Size(max = 50) String key,
        @Schema(title = "description") @Size(max = 500) String description) {}
