package com.hmg.role.rbac.policy.dto;

import static com.hmg.role.util.Constants.MAX_500_SIZE;

import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DeleteBulkPolicyDto(
        @Schema(title = "keys") @NoDuplicateValues @NotEmpty @Size(max = MAX_500_SIZE)
                List<@NotBlank String> keys) {}
