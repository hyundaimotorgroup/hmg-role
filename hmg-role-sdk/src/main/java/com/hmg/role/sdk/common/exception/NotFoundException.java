package com.hmg.role.sdk.common.exception;

public class NotFoundException extends HmgRoleException {

    public NotFoundException(String message) {
        super(404, message);
    }
}
