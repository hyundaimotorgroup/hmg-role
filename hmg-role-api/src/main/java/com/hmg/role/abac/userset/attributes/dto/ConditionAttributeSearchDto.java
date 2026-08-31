package com.hmg.role.abac.userset.attributes.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class ConditionAttributeSearchDto extends PageRequestDto {
    @Parameter private final String keyLike;
}
