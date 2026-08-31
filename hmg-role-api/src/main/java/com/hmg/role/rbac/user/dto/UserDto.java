package com.hmg.role.rbac.user.dto;

import com.hmg.role.rbac.userscoperole.dto.UserScopeRoleDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record UserDto(
        @Schema(title = "User Key") String key,
        @Schema(title = "User name") String name,
        @Schema(title = "User metadata") Map<String, String> metadata,
        @Schema(title = "Scope roles") List<UserScopeRoleDto> scopeRoles,
        @Schema(title = "Created at") String createdAt,
        @Schema(title = "Created by") String createdBy,
        @Schema(title = "Updated at") String updatedAt,
        @Schema(title = "Updated by") String updatedBy) {}
