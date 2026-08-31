package com.hmg.role.util.exceptions;

import org.springframework.http.HttpStatus;

public class MinimumKeywordLengthException extends GlobalException {
    public MinimumKeywordLengthException() {
        super(HttpStatus.BAD_REQUEST, "The keyword must be at least 3 characters long");
    }
}
