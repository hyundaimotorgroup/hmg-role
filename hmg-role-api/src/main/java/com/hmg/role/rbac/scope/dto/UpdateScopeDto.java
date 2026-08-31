package com.hmg.role.rbac.scope.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateScopeDto(@Schema(title = "Scope Name") @NotBlank @Size(max = 30) String name) {}
