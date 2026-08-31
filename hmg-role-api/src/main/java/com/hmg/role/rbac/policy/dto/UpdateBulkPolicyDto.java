package com.hmg.role.rbac.policy.dto;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateBulkPolicyDto(
        @NotBlank @Schema(title = "key") String key,
        @Schema(title = "description") String description,
        @NotBlank @Schema(title = "resourceType") String resourceType,
        @Schema(title = "scope") String scope,
        @Size(max = MAX_LIST_SIZE) @NotEmpty @NoDuplicateValues @Schema(title = "actions")
                List<@NotBlank String> actions,
        @Size(max = MAX_LIST_SIZE) @NotEmpty @NoDuplicateValues @Schema(title = "roles")
                List<@NotBlank String> roles,
        @NotNull @Schema(title = "effect") Effect effect)
        implements IPolicyModificationDto {}
