package com.hmg.role.abac.resourceset.dto;

import static com.hmg.role.util.Constants.MAX_500_SIZE;
import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.util.enums.ConditionGroupOperator;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record UpdateResourceSetDto(
        @Schema
                @Size(max = MAX_LIST_SIZE, message = "Name Size should not exceed 100 characters")
                @NotBlank(message = "name is required")
                String name,
        @Schema @Size(max = MAX_500_SIZE) String description,
        @Schema @NotNull ConditionGroupOperator conditionGroupOperator,
        @Schema @NotEmpty List<@NotNull @Valid ConditionDto> conditionGroup,
        @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                List<@NotBlank String> actions) {}
