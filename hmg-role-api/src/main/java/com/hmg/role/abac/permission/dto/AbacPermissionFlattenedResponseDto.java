package com.hmg.role.abac.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AbacPermissionFlattenedResponseDto {
    @Schema private String resourceSet;

    @Schema private String scope;

    @Schema private String userSet;

    @Schema private String action;

    @Schema private String effect;
}
