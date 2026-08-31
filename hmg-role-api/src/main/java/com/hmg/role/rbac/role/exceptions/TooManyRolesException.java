package com.hmg.role.rbac.role.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class TooManyRolesException extends BadRequestException {
    public TooManyRolesException() {
        super("Too many roles defined for the project");
    }
}
