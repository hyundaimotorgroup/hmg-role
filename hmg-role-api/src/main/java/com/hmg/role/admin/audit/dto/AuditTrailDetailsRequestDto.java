package com.hmg.role.admin.audit.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AuditTrailDetailsRequestDto(
        @NotBlank
                @Parameter(
                        description = "Object commit id",
                        schema = @Schema(type = "string"),
                        required = true)
                String commitId,
        @NotBlank
                @Parameter(
                        description = "Object entity path",
                        schema = @Schema(type = "string"),
                        required = true)
                String entityPath) {}
