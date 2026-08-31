package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.common.enums.Effect;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;

public interface PermissionFlatResponse
        extends ResourceTypeModel, ResourceActionModel, ScopeModel, RoleModel {

    Effect getEffect();
}
