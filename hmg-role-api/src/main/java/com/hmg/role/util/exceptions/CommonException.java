package com.hmg.role.util.exceptions;

import com.hmg.role.util.enums.ErrorType;
import com.hmg.role.util.interfaces.Exceptionable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommonException extends RuntimeException implements Exceptionable {
    protected final ErrorType errorType;
    protected final String message;

    @Override
    public String getName() {
        return errorType.name();
    }
}
