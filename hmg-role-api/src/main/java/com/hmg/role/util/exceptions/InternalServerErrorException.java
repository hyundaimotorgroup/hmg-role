package com.hmg.role.util.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends GlobalException {
    public InternalServerErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

    public InternalServerErrorException(HttpStatus status, String message) {
        super(status, message);
    }
}
