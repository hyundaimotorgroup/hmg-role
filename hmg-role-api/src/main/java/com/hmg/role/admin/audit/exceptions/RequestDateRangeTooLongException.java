package com.hmg.role.admin.audit.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class RequestDateRangeTooLongException extends BadRequestException {
    public RequestDateRangeTooLongException() {
        super("Request has too long of a range date");
    }
}
