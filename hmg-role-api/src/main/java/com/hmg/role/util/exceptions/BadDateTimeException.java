package com.hmg.role.util.exceptions;

public class BadDateTimeException extends BadRequestException {
    public BadDateTimeException() {
        super("Incorrect date format or value");
    }
}
