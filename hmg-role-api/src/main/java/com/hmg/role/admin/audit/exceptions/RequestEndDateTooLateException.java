package com.hmg.role.admin.audit.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class RequestEndDateTooLateException extends BadRequestException {
    public RequestEndDateTooLateException() {
        super("Request has end date later than today");
    }
}
