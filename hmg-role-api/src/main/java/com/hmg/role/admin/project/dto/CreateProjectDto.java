package com.hmg.role.admin.project.dto;

import com.hmg.role.admin.project.enums.CompanyName;
import com.hmg.role.admin.project.enums.OperatingCountry;
import com.hmg.role.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateProjectDto(
        @NotBlank
                @Pattern(regexp = Constants.ALPHA_DASH_UNDERSCORE_REGEX_PATTERN)
                @Schema(title = "Project Key")
                String key,
        @Size(max = 50, message = "Maximum Length Name is 50 Characters")
                @NotBlank
                @Schema(title = "Project Name")
                String name,
        @Size(max = 100, message = "Maximum Length Description is 100 Characters")
                @Schema(title = "Project Description")
                String description,
        @NotNull(message = "Company name can't be null")
                @Schema(type = "string", title = "Company name")
                CompanyName company,
        @NotNull(message = "Operating country can't be null")
                @Schema(type = "string", title = "Operating country")
                OperatingCountry operatingCountry,
        @NotNull
                @Schema(
                        type = "boolean",
                        title = "Is the project handles PII itself (not handled by hmgAdmin)?")
                Boolean personalDataSelfHandled,
        @NotBlank(message = "Service consent is required")
                @Pattern(
                        regexp = Constants.HTTP_URI_REGEX_PATTERN,
                        message = "Service consent history must be a webpage URL link")
                @Schema(type = "string", title = "Service consent agreement")
                String serviceConsentHistoryUrl) {}
