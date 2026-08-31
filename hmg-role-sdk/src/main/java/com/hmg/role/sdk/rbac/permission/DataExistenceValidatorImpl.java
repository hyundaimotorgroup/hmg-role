package com.hmg.role.sdk.rbac.permission;

import com.hmg.role.sdk.common.util.CollectionUtils;
import com.hmg.role.sdk.common.util.StringUtils;
import com.hmg.role.sdk.rbac.permission.exception.ActionNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.RoleNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.ScopeNotFoundException;
import com.hmg.role.sdk.rbac.permission.model.*;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionSetModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.RoleSetModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.spi.ResourceActionProvider;
import com.hmg.role.sdk.rbac.permission.spi.RoleProvider;
import com.hmg.role.sdk.rbac.permission.spi.ScopeProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Builder
public class DataExistenceValidatorImpl implements DataExistenceValidator {

    private final RoleProvider roleProvider;
    private final ScopeProvider scopeProvider;
    private final ResourceActionProvider resourceActionProvider;

    @Override
    public void validateRole(RoleModel roleModel) throws RoleNotFoundException {
        validateRole(() -> Collections.singleton(roleModel.getRoleKey()));
    }

    @Override
    public void validateRole(RoleSetModel roleSetModel) throws RoleNotFoundException {

        Set<String> roleKeysFromReq = roleSetModel.getRoleKeys();

        Set<String> roleKeysFromDb =
                roleProvider
                        .findRolesByKeys(roleKeysFromReq)
                        .map(RoleModel::getRoleKey)
                        .collect(Collectors.toSet());

        // validate the roles
        if (roleKeysFromDb.size() < roleKeysFromReq.size()) {

            Set<String> roleKeysNotFound =
                    CollectionUtils.getLeftDifference(roleKeysFromReq, roleKeysFromDb);

            throw new RoleNotFoundException(roleKeysNotFound);
        }
    }

    @Override
    public void validateScope(Collection<? extends ScopeModel> scopeModelSetReq)
            throws ScopeNotFoundException {
        Set<String> scopeKeysReq =
                scopeModelSetReq.stream()
                        .map(ScopeModel::getScopeKey)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toSet());
        validateScopeKeys(scopeKeysReq);
    }

    private void validateScopeKeys(Set<String> scopeKeysReq) throws ScopeNotFoundException {

        if (scopeKeysReq.isEmpty()) {
            return;
        }

        Set<String> scopeKeysFromDb =
                scopeProvider
                        .findScopesByKeys(scopeKeysReq)
                        .map(ScopeModel::getScopeKey)
                        .collect(Collectors.toSet());

        if (scopeKeysFromDb.size() < scopeKeysReq.size()) {

            Set<String> scopeKeysNotFound =
                    CollectionUtils.getLeftDifference(scopeKeysReq, scopeKeysFromDb);

            throw new ScopeNotFoundException(scopeKeysNotFound);
        }
    }

    @Override
    public void validateResource(
            ResourceTypeModel resourceType, ResourceActionSetModel actionSetModel)
            throws ActionNotFoundException {

        Set<String> actionNamesFromReq = actionSetModel.getActionNames();

        if (actionNamesFromReq.contains("*")) {
            // skip validation for wildcard
            log.debug(
                    "skip validation for wildcard actions, resourceTypeKey={}",
                    resourceType.getResourceTypeKey());
            return;
        }

        Set<String> actionFromDb =
                resourceActionProvider
                        .findActionsByTypeAndNames(resourceType, actionSetModel)
                        .map(ResourceActionModel::getActionName)
                        .collect(Collectors.toSet());

        if (actionFromDb.size() < actionNamesFromReq.size()) {

            Set<String> actionNamesNotFound =
                    CollectionUtils.getLeftDifference(actionNamesFromReq, actionFromDb);

            throw new ActionNotFoundException(
                    resourceType.getResourceTypeKey(), actionNamesNotFound);
        }
    }
}
