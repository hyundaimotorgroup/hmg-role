package com.hmg.role.sdk.rbac.permission;

import com.hmg.role.sdk.rbac.permission.exception.ActionNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.ResourceTypeNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.RoleNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.ScopeNotFoundException;
import com.hmg.role.sdk.rbac.permission.model.*;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionSetModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.RoleSetModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import java.util.*;
import java.util.Collection;
import java.util.Collections;

public interface DataExistenceValidator {

    void validateRole(RoleModel roleModel) throws RoleNotFoundException;

    void validateRole(RoleSetModel roleSetModel) throws RoleNotFoundException;

    default void validateScope(ScopeModel scopeModel) throws ScopeNotFoundException {
        validateScope(Collections.singleton(scopeModel));
    }

    void validateScope(Collection<? extends ScopeModel> scopeModelSetReq)
            throws ScopeNotFoundException;

    void validateResource(ResourceTypeModel resourceType, ResourceActionSetModel actionSetModel)
            throws ActionNotFoundException, ResourceTypeNotFoundException;
}
