package com.hmg.role.sdk.rbac.permission.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionFlatRequestDto implements PermissionFlatRequestByRole {

    private String scopeKey;
    private String userKey;
    private String roleKey;
    @NotBlank private String actionName;
    private String resourceId;
    @NotBlank private String resourceTypeKey;
}
