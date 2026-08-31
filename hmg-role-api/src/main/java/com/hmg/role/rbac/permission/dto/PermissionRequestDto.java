package com.hmg.role.rbac.permission.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmg.role.util.Constants;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public record PermissionRequestDto(
        @Schema @NotNull @Valid PermissionRequestUserDto user,
        @Schema @NotEmpty List<@NotNull @Valid ResourceActionsDto> resources) {

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PermissionRequestUserDto(
            @Schema String key, @Schema List<String> roles, @Schema String scope) {
        @JsonAnySetter
        public void capture(String key, Object value) {
            PermissionRequestDto.capture(key, value);
        }
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceActionsDto(
            @Schema @NotNull @Valid ResourceRequestDto resource,
            @Schema @NotEmpty @NoDuplicateValues List<@NotBlank String> actions) {

        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ResourceRequestDto(@NotBlank @Schema String type, @Schema String scope) {
            @JsonAnySetter
            public void capture(String key, Object value) {
                PermissionRequestDto.capture(key, value);
            }
        }

        @JsonAnySetter
        public void capture(String key, Object value) {
            PermissionRequestDto.capture(key, value);
        }
    }

    @JsonAnySetter
    public static void capture(String key, Object value) {
        log.warn(
                "Extra fields during permission checking, key: {}, value: {}, traceId: {}",
                key,
                value,
                MDC.get(Constants.MDC_KEY_TRACE_ID));
    }
}
