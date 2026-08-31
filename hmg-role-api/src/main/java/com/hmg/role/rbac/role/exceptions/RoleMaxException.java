package com.hmg.role.rbac.role.exceptions;

import com.hmg.role.util.exceptions.GlobalException;
import org.springframework.http.HttpStatus;

public class RoleMaxException extends GlobalException {
    public RoleMaxException() {
        super(HttpStatus.BAD_REQUEST, "The maximum role data 100 length exceed");
    }
}
