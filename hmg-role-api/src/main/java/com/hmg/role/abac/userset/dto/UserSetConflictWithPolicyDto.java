package com.hmg.role.abac.userset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UserSetConflictWithPolicyDto(
        @Schema(title = "User Set Key") String userKey,
        @Schema(title = "User Set Name") String userSetName) {}
