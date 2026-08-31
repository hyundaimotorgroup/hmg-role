package com.hmg.role.rbac.permission.exceptions;

import com.hmg.role.util.exceptions.GlobalException;
import org.springframework.http.HttpStatus;

public class RoleOrUserKeyIsRequiredException extends GlobalException {

    public RoleOrUserKeyIsRequiredException() {
        super(HttpStatus.BAD_REQUEST, "Any of Role or User Is Required");
    }
}
