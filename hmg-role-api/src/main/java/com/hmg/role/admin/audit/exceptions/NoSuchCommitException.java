package com.hmg.role.admin.audit.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class NoSuchCommitException extends BadRequestException {
    public NoSuchCommitException() {
        super("No such commit");
    }
}
