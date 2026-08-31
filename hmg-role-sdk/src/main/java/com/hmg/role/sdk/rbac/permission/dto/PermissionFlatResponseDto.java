package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.common.enums.Effect;
import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionFlatResponseDto implements PermissionFlatResponse {

    private String resourceId, resourceTypeKey, scopeKey, roleKey, actionName;
    private Effect effect;
}
