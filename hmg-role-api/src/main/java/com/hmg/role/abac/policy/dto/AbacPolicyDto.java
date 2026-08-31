package com.hmg.role.abac.policy.dto;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.util.Constants;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record AbacPolicyDto(
        @Schema
                @Pattern(regexp = Constants.ALPHA_DASH_UNDERSCORE_REGEX_PATTERN)
                @NotBlank
                @Size(max = Constants.MAX_LIST_SIZE)
                String key,
        @Schema @NotBlank @Size(max = Constants.MAX_LIST_SIZE) String scope,
        @Schema @Size(max = Constants.MAX_LIST_SIZE) String description,
        @Schema @NotBlank @Size(max = Constants.MAX_LIST_SIZE) String resourceSet,
        @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                List<@NotBlank String> actions,
        @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                List<@NotBlank String> userSets,
        @Schema @NotNull Effect effect) {}
