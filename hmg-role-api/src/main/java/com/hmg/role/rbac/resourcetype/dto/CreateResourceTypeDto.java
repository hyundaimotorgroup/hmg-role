package com.hmg.role.rbac.resourcetype.dto;

import com.hmg.role.util.Constants;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record CreateResourceTypeDto(
        @Size(
                        max = Constants.MAX_500_SIZE,
                        message = "Maximum Length Description is 500 Characters")
                @Schema(title = "Resource Type Description")
                String description,
        @NotBlank
                @Pattern(regexp = Constants.REQUEST_DTO_URI_REGEX_PATTERN)
                @Schema(title = "Resource Type Key")
                @Size(
                        max = Constants.MAX_100_SIZE,
                        message = "Maximum Length Key is 100 Characters")
                String key,
        @NotEmpty
                @NoDuplicateValues(message = "must not contain duplicate actions")
                @Schema(title = "Resource Action")
                List<
                                @NotBlank
                                @Pattern(
                                        regexp = "^(?!\\s*\\*\\s*$).*$",
                                        message = "Value must not be '*'")
                                String>
                        actions,
        @NotBlank
                @Size(
                        max = Constants.MAX_50_SIZE,
                        message = "Maximum Resource Type Name is 50 Characters")
                String name,
        @NoDuplicateValues
                List<
                                @Size(
                                        max = Constants.MAX_50_SIZE,
                                        message = "Maximum Length Tag is 50 Characters")
                                String>
                        tags,
        String parentKey) {}
