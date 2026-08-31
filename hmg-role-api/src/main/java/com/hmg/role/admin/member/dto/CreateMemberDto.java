package com.hmg.role.admin.member.dto;

import com.hmg.role.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateMemberDto(
        @NotBlank
                @Pattern(regexp = Constants.ALPHA_DASH_UNDERSCORE_REGEX_PATTERN)
                @Schema(title = "Key")
                String key,
        @NotBlank @Schema(title = "Name") String name,
        @Schema(title = "API Key") UUID apiKey,
        @Schema(title = "Member Description") String description) {}
