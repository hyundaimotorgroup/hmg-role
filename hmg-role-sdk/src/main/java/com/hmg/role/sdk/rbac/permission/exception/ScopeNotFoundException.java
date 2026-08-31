package com.hmg.role.sdk.rbac.permission.exception;

import com.hmg.role.sdk.common.exception.NotFoundException;
import java.util.Collection;
import lombok.Getter;

@Getter
public class ScopeNotFoundException extends NotFoundException {

    private final Collection<String> scopeKeys;

    public ScopeNotFoundException(Collection<String> scopeKeys) {
        super("Scope not found for key='" + scopeKeys + "'");
        this.scopeKeys = scopeKeys;
    }
}
