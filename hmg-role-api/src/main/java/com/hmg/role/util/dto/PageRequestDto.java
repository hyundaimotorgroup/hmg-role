package com.hmg.role.util.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@Schema(title = "Page DTO")
public class PageRequestDto {
    @Parameter(description = "Page size", example = "10")
    @Min(2)
    private Integer size = 10;

    @Parameter(description = "Page Number", example = "0")
    @Min(0)
    private Integer page = 0;

    public Pageable pageRequest() {
        return PageRequest.of(page, size);
    }
}
