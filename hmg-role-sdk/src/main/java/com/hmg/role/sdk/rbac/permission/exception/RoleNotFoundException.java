package com.hmg.role.sdk.rbac.permission.exception;

import com.hmg.role.sdk.common.exception.NotFoundException;
import java.util.Collection;
import lombok.Getter;

@Getter
public class RoleNotFoundException extends NotFoundException {

    private final Collection<String> roleKeys;

    public RoleNotFoundException(Collection<String> roleKeys) {
        super("Role not found for key='" + roleKeys + "'");
        this.roleKeys = roleKeys;
    }
}
