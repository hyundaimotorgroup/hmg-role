package com.hmg.role.abac.userset.dto;

import static com.hmg.role.util.Constants.MAX_500_SIZE;
import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.util.enums.ConditionGroupOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UpdateUserSetDto(
        @Schema @Length(max = MAX_LIST_SIZE) String name,
        @Schema @Length(max = MAX_500_SIZE) String description,
        @Schema @NotNull ConditionGroupOperator conditionGroupOperator,
        @Schema @NotEmpty @Size(max = MAX_LIST_SIZE) List<@NotNull ConditionDto> conditionGroup) {}
