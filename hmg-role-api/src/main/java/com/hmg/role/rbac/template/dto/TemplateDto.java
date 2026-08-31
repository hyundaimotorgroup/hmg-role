package com.hmg.role.rbac.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record TemplateDto(
        @Schema(name = "Value") String value, @Schema(name = "Type") String type) {}
