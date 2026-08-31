package com.hmg.role.sdk.common.exception;

public class BadRequestException extends HmgRoleException {

    public BadRequestException(String message) {
        super(400, message);
    }
}
