package com.hmg.role.rbac.user.dto;

import com.hmg.role.rbac.userscoperole.dto.UpdateUserScopeRoleDto;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record UpdateBulkUserDto(
        @NotBlank @Schema(title = "User Key") String key,
        @Schema(title = "User name") String name,
        @Schema(title = "User metadata") Map<String, String> metadata,
        @Schema(title = "Scope role") @NoDuplicateValues
                List<@Valid UpdateUserScopeRoleDto> scopeRoles) {}
