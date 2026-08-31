package com.hmg.role.util.exceptions;

public class InvalidServiceConsentHistoryException extends BadRequestException {
    public InvalidServiceConsentHistoryException() {
        super("Invalid service consent history");
    }
}
