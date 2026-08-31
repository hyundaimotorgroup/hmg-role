package com.hmg.role.abac.logicalexpression.dto;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmg.role.util.enums.ConditionGroupOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DryRunDto(
        // TODO rename to EvaluationDto for use on both UserSet and ResourceSet
        @Schema @NotNull ConditionGroupOperator conditionGroupOperator,
        @Schema @NotEmpty @Size(max = MAX_LIST_SIZE)
                List<@Valid @NotNull ConditionDto> conditionGroup,
        @Schema @NotEmpty @Size(max = MAX_LIST_SIZE)
                Map<String, @NotNull Object> attributeValues) {}
