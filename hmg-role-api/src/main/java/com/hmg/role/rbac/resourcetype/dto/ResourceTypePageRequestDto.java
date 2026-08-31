package com.hmg.role.rbac.resourcetype.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResourceTypePageRequestDto extends PageRequestDto {
    String andActionLike;

    // TODO: [suggestion] Rename the parameter with clear statement variable (eg. Type ->
    // fieldSearch or Keyword -> fieldValue) for extensible
    @Parameter(
            in = ParameterIn.QUERY,
            description = "Select Type to filter",
            schema =
                    @Schema(
                            type = "string",
                            allowableValues = {"name_tag", "name", "tag", "key", "action"}))
    String type;

    @Parameter(description = "Search Keyword")
    private String keyword;

    @Parameter(
            in = ParameterIn.QUERY,
            description = "Select source",
            schema =
                    @Schema(
                            type = "string",
                            allowableValues = {"resource_type", "policy"}))
    private String source;
}
