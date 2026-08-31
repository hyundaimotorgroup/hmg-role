package com.hmg.role.rbac.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UserConflictWithRoleDto(
        @Schema(title = "User Key") String key, @Schema(title = "User name") String name) {}
