package com.hmg.role.rbac.policy.dto;

import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
public record UpdatePolicyDto(
        @Schema(name = "description") String description,
        @NotBlank @Schema(name = "resourceType") String resourceType,
        @Schema(name = "scope") String scope,
        @NotEmpty @NoDuplicateValues @Schema(name = "actions") List<@NotBlank String> actions,
        @NotEmpty @NoDuplicateValues @Schema(name = "roles") List<@NotBlank String> roles,
        @NotNull @Schema(name = "effect") Effect effect) {}
