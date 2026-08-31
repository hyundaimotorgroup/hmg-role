package com.hmg.role.sdk.rbac.permission.exception;

import com.hmg.role.sdk.common.exception.NotFoundException;
import lombok.Getter;

@Getter
public class RoleByUserAndScopeNotFoundException extends NotFoundException {

    private final String userKey, scopeKey;

    public RoleByUserAndScopeNotFoundException(String userKey, String scopeKey) {
        super(
                String.format(
                        "Role not found for userKey='%s' and scopeKey='%s'", userKey, scopeKey));
        this.userKey = userKey;
        this.scopeKey = scopeKey;
    }
}
