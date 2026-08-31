package com.hmg.role.abac.policy.dto;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.util.Constants;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UpdateAbacPolicyDto(
        @Schema @NotBlank @Size(max = Constants.MAX_LIST_SIZE) String scope,
        @Schema @Length(max = MAX_LIST_SIZE) String description,
        @Schema @NotBlank @Length(max = MAX_LIST_SIZE) String resourceSet,
        @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                List<@NotBlank @Length(max = MAX_LIST_SIZE) String> actions,
        @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                List<@NotBlank @Length(max = MAX_LIST_SIZE) String> userSets,
        @Schema @NotNull Effect effect) {}
