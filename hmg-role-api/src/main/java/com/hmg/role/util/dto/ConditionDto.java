package com.hmg.role.util.dto;

import com.hmg.role.util.enums.ConditionOperator;
import com.hmg.role.util.enums.OperandDataType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Deprecated(forRemoval = true)
@Builder
public record ConditionDto(
        // TODO remove and replace with ConditionDto
        @Schema @NotNull OperandDto left,
        @Schema @NotNull ConditionOperator operator,
        @Schema @NotNull OperandDto right) {

    @Builder
    // TODO remove and replace with OperandDto
    public record OperandDto(
            @Schema @NotBlank String operand, @Schema @NotNull OperandDataType type) {}
}
