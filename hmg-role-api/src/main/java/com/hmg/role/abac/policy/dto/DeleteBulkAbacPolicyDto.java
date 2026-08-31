package com.hmg.role.abac.policy.dto;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DeleteBulkAbacPolicyDto(
        @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                List<@NotBlank String> keys) {}
