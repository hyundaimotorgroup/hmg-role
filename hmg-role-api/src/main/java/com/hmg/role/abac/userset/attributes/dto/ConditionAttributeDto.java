package com.hmg.role.abac.userset.attributes.dto;

import com.hmg.role.util.Constants;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record ConditionAttributeDto(
        // can't reuse OperandDto since the API uses "key" instead of "operand"
        @Schema
                @NotBlank
                @Length(max = Constants.MAX_40_SIZE)
                @Pattern(regexp = Constants.ALPHA_DASH_UNDERSCORE_REGEX_PATTERN)
                String key,
        @Schema @NotNull OperandDataType dataType,
        @Schema @NotNull OperandType type) {}
