package com.hmg.role.abac.resourceset.dto;

import static com.hmg.role.util.Constants.MAX_500_SIZE;
import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.util.enums.ConditionGroupOperator;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import com.hmg.role.util.validation.annotations.ValidEntityKey;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record ResourceSetDto(
        @Schema @ValidEntityKey String key,
        @Schema
                @Length(
                        max = MAX_LIST_SIZE,
                        message = "Name length should not exceed 100 characters")
                @NotBlank(message = "name is required")
                String name,
        @Schema @Length(max = MAX_500_SIZE) String description,
        @Schema String parent,
        @Schema @NotNull ConditionGroupOperator conditionGroupOperator,
        @Schema @NotEmpty List<@NotNull @Valid ConditionDto> conditionGroup,
        @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                List<@NotBlank String> actions,
        @Schema String createdAt,
        @Schema String createdBy,
        @Schema String updatedAt,
        @Schema String updatedBy) {}
