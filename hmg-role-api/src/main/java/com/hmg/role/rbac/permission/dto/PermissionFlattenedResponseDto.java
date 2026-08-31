package com.hmg.role.rbac.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PermissionFlattenedResponseDto {

    @Schema private String type;

    @Schema private String scope;

    @Schema private String role;

    @Schema private String action;

    @Schema private String effect;
}
