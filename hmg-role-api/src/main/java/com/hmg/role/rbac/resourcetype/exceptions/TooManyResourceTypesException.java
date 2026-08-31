package com.hmg.role.rbac.resourcetype.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class TooManyResourceTypesException extends BadRequestException {
    public TooManyResourceTypesException() {
        super("Too many resource types defined for the project");
    }
}
