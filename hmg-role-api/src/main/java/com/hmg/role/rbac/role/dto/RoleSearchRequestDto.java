package com.hmg.role.rbac.role.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RoleSearchRequestDto extends PageRequestDto {

    @Parameter(
            in = ParameterIn.QUERY,
            description = "Select Type to filter",
            schema =
                    @Schema(
                            type = "string",
                            allowableValues = {"name", "key"}))
    String type;

    @Parameter(description = "Search Keyword")
    private String keyword;
}
