package com.hmg.role.rbac.scope.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateScopeDto(
        @Schema(title = "Scope Key") @NotBlank @Size(max = 30) @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
                String key,
        @Schema(title = "Scope Name") @NotBlank @Size(max = 30) String name) {}
