package com.hmg.role.util.exceptions;

public class InvalidOperatingCountryException extends BadRequestException {
    public InvalidOperatingCountryException() {
        super("Invalid country name");
    }
}
