package com.hmg.role.abac.userset.exceptions;

import com.hmg.role.util.exceptions.EmptyRequestException;

public class UserSetIsEmptyException extends EmptyRequestException {
    public UserSetIsEmptyException() {
        super("Userset is empty");
    }
}
