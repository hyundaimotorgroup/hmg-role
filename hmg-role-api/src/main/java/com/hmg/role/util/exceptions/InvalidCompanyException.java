package com.hmg.role.util.exceptions;

public class InvalidCompanyException extends BadRequestException {
    public InvalidCompanyException() {
        super("Invalid company name");
    }
}
