package com.hmg.role.abac.permission.dto;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbacPermissionRequestDto(
        @Schema @NotNull @Valid AbacInstanceValuesDto user,
        @Schema @NotEmpty @Size(max = MAX_LIST_SIZE)
                List<@NotNull @Valid AbacResourceActionsDto> resources) {

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AbacInstanceValuesDto(
            @Schema @NotBlank String scope, @Schema @NotEmpty Map<String, Object> attributes) {}

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AbacResourceActionsDto(
            @Schema @NotNull @Valid AbacInstanceValuesDto resource,
            @Schema @NotEmpty @NoDuplicateValues @Size(max = MAX_LIST_SIZE)
                    List<@NotBlank String> actions) {}
}
