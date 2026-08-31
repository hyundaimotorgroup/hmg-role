package com.hmg.role.abac.common.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Schema
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonAbacSearchDto extends PageRequestDto {
    @Parameter(description = "prefix key match")
    private String keyLike;

    @Parameter(description = "prefix name match")
    private String nameLike;
}
