package com.hmg.role.rbac.scope.exceptions;

import com.hmg.role.rbac.scope.dto.ScopeConflictDetailDto;
import com.hmg.role.util.exceptions.BeingUsedException;
import lombok.Getter;

@Getter
public class ScopeBeingUsedException extends BeingUsedException {

    public ScopeBeingUsedException(ScopeConflictDetailDto detail) {
        super("Scope is being used in Users or Policies", detail);
    }

    public ScopeBeingUsedException(Object data) {
        super("Scope is being used in Project", data);
    }
}
