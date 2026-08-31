package com.hmg.role.rbac.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record RoleDto(
        @Schema(title = "role name") String name,
        @Schema(title = "role description") String description,
        @Schema(title = "role key") String key,
        @Schema(title = "created at") String createdAt,
        @Schema(title = "created by") String createdBy,
        @Schema(title = "updated at") String updatedAt,
        @Schema(title = "updated by") String updatedBy) {}
