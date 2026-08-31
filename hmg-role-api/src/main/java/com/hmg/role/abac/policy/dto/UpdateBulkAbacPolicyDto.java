package com.hmg.role.abac.policy.dto;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.util.Constants;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import com.hmg.role.util.validation.annotations.ValidEntityKey;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateBulkAbacPolicyDto(
        @Schema @ValidEntityKey String key,
        @Schema @NotBlank @Size(max = Constants.MAX_LIST_SIZE) String scope,
        @Schema String description,
        @Schema @NotBlank String resourceSet,
        @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                List<@NotBlank String> actions,
        @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                List<@NotBlank String> userSets,
        @Schema @NotNull Effect effect) {}
