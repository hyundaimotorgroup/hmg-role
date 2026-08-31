package com.hmg.role.rbac.permission.exceptions;

import com.hmg.role.util.exceptions.GlobalException;
import org.springframework.http.HttpStatus;

public class RoleIsRequiredException extends GlobalException {

    public RoleIsRequiredException() {
        super(HttpStatus.BAD_REQUEST, "Role Is Required");
    }
}
