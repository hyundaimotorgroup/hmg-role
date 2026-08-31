package com.hmg.role.abac.logicalexpression.dto;

import com.hmg.role.util.enums.ConditionOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ConditionDto(
        @Schema @NotNull OperandDto left,
        @Schema @NotNull ConditionOperator operator,
        @Schema @NotNull OperandDto right) {}
