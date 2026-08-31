package com.hmg.role.rbac.resourcetype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "Resource Type DTO")
public record ResourceTypeDto(
        @Schema String description,
        @Schema String key,
        @Schema List<String> actions,
        @Schema String name,
        @Schema List<String> tags,
        @Schema List<ResourceTypeDto> children,
        //      TODO: [Suggestion] consider to use only the resource dataType key and parent
        @Schema List<String> subResourceTypeKeys,
        @Schema String parentKey,
        @Schema long childrenCount,
        @Schema String createdAt,
        @Schema String updatedAt) {}
