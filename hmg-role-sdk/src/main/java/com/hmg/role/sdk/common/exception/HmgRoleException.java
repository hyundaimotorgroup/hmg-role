package com.hmg.role.sdk.common.exception;

import lombok.Getter;

@Getter
public class HmgRoleException extends Exception {

    private final int statusCode;

    public HmgRoleException(int status, String message) {
        super(message);
        this.statusCode = status;
    }
}
