package com.hmg.role.admin.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UpdateMemberDto(
        @NotBlank @Schema(title = "Name") String name,
        @NotNull @Schema(title = "API Key") UUID apiKey,
        @Schema(title = "Member Description") String description) {}
