package com.hmg.role.util.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmg.role.admin.project.dto.ProjectDto;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(title = "Metadata")
public record Metadata(
        @Schema(title = "Total Count") Long totalCount,
        @Schema(title = "Total Page Count") Integer totalPageCount,
        @Schema(title = "Page size") Integer size,
        // can't append Project in a new class since it will break ListResponseDto
        @Schema(title = "User project") ProjectDto project) {

    public static Metadata create(Page<?> page) {
        return new Metadata(page.getTotalElements(), page.getTotalPages(), page.getSize(), null);
    }

    public static Metadata create(Page<?> page, ProjectDto projectDto) {
        return new Metadata(
                page.getTotalElements(), page.getTotalPages(), page.getSize(), projectDto);
    }

    public static Metadata create(Page<?> page, long totalCount) {
        return new Metadata(totalCount, page.getTotalPages(), page.getSize(), null);
    }
}
