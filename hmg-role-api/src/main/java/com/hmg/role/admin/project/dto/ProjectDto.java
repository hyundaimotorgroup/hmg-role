package com.hmg.role.admin.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import lombok.Builder;

@Builder
public record ProjectDto(
        @Schema(name = "project_key") String key,
        @Schema(name = "project_name") String name,
        @Schema(name = "project_description") String description,
        @Schema(name = "Company name") String company,
        @Schema(name = "Operating country") String operatingCountry,
        @Schema(name = "Is the project handles PII itself (not handled by hmgAdmin)?")
                Boolean personalDataSelfHandled,
        @Schema(name = "Service consent history") String serviceConsentHistoryUrl,
        @Schema(name = "created_at") ZonedDateTime createdAt,
        @Schema(name = "created_by") String createdBy,
        @Schema(name = "updated_at") ZonedDateTime updatedAt,
        @Schema(name = "updated_by") String updatedBy) {}
