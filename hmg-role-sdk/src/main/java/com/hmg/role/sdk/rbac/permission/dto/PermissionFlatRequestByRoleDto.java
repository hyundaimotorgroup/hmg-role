package com.hmg.role.sdk.rbac.permission.dto;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionFlatRequestByRoleDto implements PermissionFlatRequestByRole {

    @NotBlank private String scopeKey;

    @NotBlank private String roleKey;

    @NotBlank private String actionName;

    @Nullable private String resourceId;

    @NotBlank private String resourceTypeKey;
}
