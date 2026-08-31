package com.hmg.role.rbac.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record RoleWithUserCountDto(
        @Schema(title = "role name") String name,
        @Schema(title = "role key") String key,
        @Schema(title = "role description") String description,
        @Schema(title = "user count") long userCount,
        @Schema(title = "created at") String createdAt,
        @Schema(title = "updated at") String updatedAt) {}
