package com.hmg.role.util.exceptions;

public class EmptyRequestException extends BadRequestException {
    public EmptyRequestException(String message) {
        super(message);
    }
}
