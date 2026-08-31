package com.hmg.role.util.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "Page DTO with Keyword")
public class PageRequestWithKeywordDto extends PageRequestDto {

    @Parameter(description = "Search Keyword")
    private String keyword;
}
