package com.hmg.role.rbac.permission.exceptions;

import com.hmg.role.util.exceptions.GlobalException;
import org.springframework.http.HttpStatus;

public class NullPermissionRequestDtoException extends GlobalException {
    public NullPermissionRequestDtoException() {
        super(HttpStatus.BAD_REQUEST, "permissionRequestUserDto is null");
    }
}
