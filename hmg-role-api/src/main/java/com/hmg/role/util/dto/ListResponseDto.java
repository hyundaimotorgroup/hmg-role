package com.hmg.role.util.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmg.role.admin.project.dto.ProjectDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Schema(title = "List Response DTO")
public record ListResponseDto<T>(
        @Schema(title = "Result") List<T> results,
        @Schema(title = "List of Metadata") Metadata metadata) {

    public static <T> ListResponseDto<T> create(Page<T> page) {
        return new ListResponseDto<>(page.getContent(), Metadata.create(page));
    }

    public static <T> ListResponseDto<T> create(Page<T> page, ProjectDto projectDto) {
        return new ListResponseDto<>(page.getContent(), Metadata.create(page, projectDto));
    }

    public static <T> ListResponseDto<T> create(List<T> page) {
        return new ListResponseDto<>(page, null);
    }

    public static <T> ListResponseDto<T> create(Page<T> page, long totalCount) {
        return new ListResponseDto<>(page.getContent(), Metadata.create(page, totalCount));
    }
}
