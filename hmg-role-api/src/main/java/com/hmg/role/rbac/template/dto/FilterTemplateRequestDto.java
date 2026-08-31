package com.hmg.role.rbac.template.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class FilterTemplateRequestDto extends PageRequestDto {

    @Parameter(description = "type")
    private String type;
}
