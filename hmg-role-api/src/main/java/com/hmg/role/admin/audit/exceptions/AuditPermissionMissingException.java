package com.hmg.role.admin.audit.exceptions;

import com.hmg.role.util.exceptions.UnauthorizedException;

public class AuditPermissionMissingException extends UnauthorizedException {
    public AuditPermissionMissingException() {
        this("Audit permission is missing");
    }

    private AuditPermissionMissingException(String message) {
        super(message);
    }

    public static AuditPermissionMissingException ofNoUserInfoVo() {
        return new AuditPermissionMissingException("Can't find user information in hmgAdmin");
    }
}
