package com.hmg.role.util.exceptions;

import org.springframework.http.HttpStatus;

public class BeingUsedException extends GlobalException {

    public BeingUsedException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public BeingUsedException(String message, Object detail) {
        super(HttpStatus.CONFLICT, message, detail);
    }
}
