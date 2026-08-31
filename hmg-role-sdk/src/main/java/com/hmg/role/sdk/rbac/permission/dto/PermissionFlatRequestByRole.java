package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

public interface PermissionFlatRequestByRole
        extends ScopeModel, RoleModel, ResourceTypeModel, ResourceActionModel {

    @NotBlank
    String getScopeKey();

    @NotBlank
    String getRoleKey();

    @NotBlank
    String getActionName();

    @Nullable
    String getResourceId();

    @NotBlank
    String getResourceTypeKey();
}
