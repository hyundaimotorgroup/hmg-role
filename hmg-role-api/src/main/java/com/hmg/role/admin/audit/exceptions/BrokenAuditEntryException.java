package com.hmg.role.admin.audit.exceptions;

import com.hmg.role.util.exceptions.InternalServerErrorException;

public class BrokenAuditEntryException extends InternalServerErrorException {
    public BrokenAuditEntryException() {
        super("Broken audit entry");
    }
}
