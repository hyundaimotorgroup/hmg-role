package com.hmg.role.abac.logicalexpression.dto;

import com.hmg.role.util.Constants;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OperandDto(
        @Schema @NotBlank @Size(max = Constants.MAX_50_SIZE) String operand,
        @Schema @NotNull OperandDataType dataType,
        @Schema @NotNull OperandType type) {}
