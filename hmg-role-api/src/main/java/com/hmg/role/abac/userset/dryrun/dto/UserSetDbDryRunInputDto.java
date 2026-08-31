package com.hmg.role.abac.userset.dryrun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record UserSetDbDryRunInputDto(
        @Schema String userSetKey,
        @Schema @NotNull @NotEmpty Map<String, Object> attributeValues) {}
