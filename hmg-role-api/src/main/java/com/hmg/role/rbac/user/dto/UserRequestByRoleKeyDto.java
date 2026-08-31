package com.hmg.role.rbac.user.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserRequestByRoleKeyDto extends PageRequestDto {

    @Parameter(description = "Search Keyword")
    private String keyword;

    @NotBlank
    @Parameter(description = "role key")
    private String roleKey;

    @Parameter private String scope;
}
