package com.hmg.role.rbac.role.exceptions;

import com.hmg.role.rbac.role.dto.RoleConflictDetailDto;
import com.hmg.role.util.exceptions.BeingUsedException;
import lombok.Getter;

@Getter
public class RoleBeingUsedException extends BeingUsedException {

    public RoleBeingUsedException(RoleConflictDetailDto detail) {
        super("Role is being used in Users or Policies", detail);
    }
}
