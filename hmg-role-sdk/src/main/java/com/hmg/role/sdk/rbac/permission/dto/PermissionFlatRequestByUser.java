package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.model.UserModel;
import javax.validation.constraints.NotBlank;

public interface PermissionFlatRequestByUser
        extends UserModel, ScopeModel, ResourceActionModel, ResourceTypeModel {

    String getScopeKey();

    String getUserKey();

    @NotBlank
    String getActionName();

    String getResourceId();

    @NotBlank
    String getResourceTypeKey();
}
