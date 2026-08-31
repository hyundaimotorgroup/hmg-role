package com.hmg.role.admin.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import lombok.Builder;

@Builder
public record MemberDto(
        @Schema(title = "Key") String key,
        @Schema(title = "Name") String name,
        @Schema(title = "API Key") String apiKey,
        @Schema(title = "Created At") ZonedDateTime createdAt,
        @Schema(title = "Created By") String createdBy,
        @Schema(title = "Updated At") ZonedDateTime updatedAt,
        @Schema(title = "Updated By") String updatedBy,
        @Schema(title = "Member Description") String description) {}
