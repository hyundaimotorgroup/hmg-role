package com.hmg.role.util.exceptions;

import org.springframework.http.HttpStatus;

public class AlreadyExistException extends GlobalException {

    public AlreadyExistException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
