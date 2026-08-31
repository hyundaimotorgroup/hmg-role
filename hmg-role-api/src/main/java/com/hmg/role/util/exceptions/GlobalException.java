package com.hmg.role.util.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GlobalException extends RuntimeException {
    // IDEA: change to `extends org.springframework.web.server.ResponseStatusException`
    // to make it more align with Spring's exception handling

    private final HttpStatus status;
    private final Object detail;

    public GlobalException(HttpStatus status, String message, Throwable cause, Object detail) {
        super(message, cause);
        this.status = status;
        this.detail = detail;
    }

    public GlobalException(HttpStatus status, String message, Throwable cause) {
        this(status, message, cause, null);
    }

    public GlobalException(HttpStatus status, String message, Object detail) {
        this(status, message, null, detail);
    }

    public GlobalException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public String getLogMessage() {
        return detail == null ? getMessage() : getMessage() + " - detail: " + detail;
    }
}
