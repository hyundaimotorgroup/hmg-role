package com.hmg.role.abac.resourceset.dto;

import com.hmg.role.abac.common.dto.CommonAbacSearchDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class SearchResourceSetDto extends CommonAbacSearchDto {
    @Parameter(description = "resource set action match")
    private String actionLike;
}
