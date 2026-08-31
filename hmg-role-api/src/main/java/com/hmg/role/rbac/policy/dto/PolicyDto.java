package com.hmg.role.rbac.policy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record PolicyDto(
        @Schema(name = "Policy Key") String key,
        @Schema(name = "description") String description,
        @Schema(name = "Scope") String scopeKey,
        @Schema(name = "Resource Type") String resourceType,
        @Schema(name = "Actions") List<String> actions,
        @Schema(name = "Roles") List<String> roles,
        @Schema(name = "Effect") String effect) {}
