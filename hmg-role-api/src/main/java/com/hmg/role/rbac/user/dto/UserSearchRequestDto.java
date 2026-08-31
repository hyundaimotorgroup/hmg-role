package com.hmg.role.rbac.user.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserSearchRequestDto extends PageRequestDto {
    @Parameter(
            in = ParameterIn.QUERY,
            description = "Select Type to filter",
            schema =
                    @Schema(
                            type = "string",
                            allowableValues = {"name", "key", "name_key", "role_name"}))
    String type; // TODO use enum instead

    @Parameter(description = "Search Keyword")
    private String keyword;

    @Parameter(description = "Filter Scope")
    private String scope;

    @Parameter(description = "Filter Role")
    private String role;
}
