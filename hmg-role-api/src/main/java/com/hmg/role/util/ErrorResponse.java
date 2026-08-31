package com.hmg.role.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmg.role.util.exceptions.CommonException;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final String message;
    private final List<ErrorDetail> errors;

    public static ErrorResponse create(CommonException commonException) {
        ErrorDetail errorDetail =
                new ErrorDetail(
                        commonException.getClass().getSimpleName(),
                        Strings.EMPTY,
                        commonException.getName());

        return new ErrorResponse(commonException.getErrorType().getMessage(), List.of(errorDetail));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    public record ErrorDetail(String resource, String field, String code) {}
}
