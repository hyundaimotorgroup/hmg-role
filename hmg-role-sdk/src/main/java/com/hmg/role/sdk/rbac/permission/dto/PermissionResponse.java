package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.common.enums.Effect;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import java.util.List;

public interface PermissionResponse extends ScopeModel, ResourceTypeModel {

    String getResourceTypeKey();

    String getScopeKey();

    List<? extends ActionEffect> getActionEffects();

    interface ActionEffect extends RoleModel, ResourceActionModel {

        String getRoleKey();

        String getActionName();

        Effect getEffect();
    }
}
