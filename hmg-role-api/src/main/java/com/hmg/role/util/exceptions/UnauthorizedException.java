package com.hmg.role.util.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends GlobalException {

    public UnauthorizedException() {
        this(null);
    }

    public UnauthorizedException(String logMessage) {
        super(HttpStatus.UNAUTHORIZED, "Unauthorized", logMessage);
    }
}
