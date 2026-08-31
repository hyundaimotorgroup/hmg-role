package com.hmg.role.rbac.role.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RoleScopeUserRequestDto extends PageRequestDto {

    @Parameter(description = "Filter by scope key")
    private String scopeKey;

    @Parameter(description = "Search by user name or user key (case-insensitive contains)")
    private String userNameOrUserKeyContains;
}
