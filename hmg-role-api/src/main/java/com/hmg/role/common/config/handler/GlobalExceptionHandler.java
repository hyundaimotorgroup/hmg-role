package com.hmg.role.common.config.handler;

import com.hmg.role.util.Constants;
import com.hmg.role.util.ErrorResponse;
import com.hmg.role.util.dto.GlobalErrorResponseDto;
import com.hmg.role.util.exceptions.CommonException;
import com.hmg.role.util.exceptions.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String validationMessage =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getField() + " " + e.getDefaultMessage())
                        .collect(Collectors.joining(", "));

        log.warn(
                "Validation failed, traceId: {}, projectKey: {}, memberKey: {}, path: {}, message: {}",
                MDC.get(Constants.MDC_KEY_TRACE_ID),
                MDC.get(Constants.MDC_KEY_PROJECT_KEY),
                MDC.get(Constants.MDC_KEY_MEMBER_KEY),
                request.getRequestURI(),
                validationMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        GlobalErrorResponseDto.builder()
                                .timestamp(
                                        OffsetDateTime.now()
                                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(validationMessage)
                                .path(request.getRequestURI())
                                .build());
    }

    @ExceptionHandler({CommonException.class})
    public ResponseEntity<ErrorResponse> handleCommonException(CommonException exception) {
        log.error(
                "occurred commonException, traceId: {}, projectKey: {}, memberKey: {}",
                MDC.get(Constants.MDC_KEY_TRACE_ID),
                MDC.get(Constants.MDC_KEY_PROJECT_KEY),
                MDC.get(Constants.MDC_KEY_MEMBER_KEY),
                exception);
        return ResponseEntity.status(exception.getErrorType().getHttpStatusType())
                .body(ErrorResponse.create(exception));
    }

    @ExceptionHandler({GlobalException.class})
    public ResponseEntity<GlobalErrorResponseDto> handleGlobalException(
            GlobalException exception, HttpServletRequest request) {
        log.error(
                "occurred globalException, traceId: {}, projectKey: {}, memberKey: {}",
                MDC.get(Constants.MDC_KEY_TRACE_ID),
                MDC.get(Constants.MDC_KEY_PROJECT_KEY),
                MDC.get(Constants.MDC_KEY_MEMBER_KEY),
                exception);

        var detail = exception.getDetail();

        var status = exception.getStatus();
        if (status == HttpStatus.UNAUTHORIZED) {
            log.debug(exception.getLogMessage());
            detail = null;
        } else if (status.is4xxClientError()) {
            log.debug(exception.getLogMessage());
        } else if (status.is5xxServerError()) {
            log.error(exception.getLogMessage(), exception);
        }

        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");

        return ResponseEntity.status(exception.getStatus())
                .body(
                        GlobalErrorResponseDto.builder()
                                .status(exception.getStatus().value())
                                .error(exception.getStatus().getReasonPhrase())
                                .message(exception.getMessage())
                                .path(request.getRequestURI())
                                .timestamp(ZonedDateTime.now().format(formatter))
                                .detail(detail)
                                .build());
    }

    private static String getRequestBody(HttpServletRequest request) {
        try {
            return request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            log.warn("Failed to read request body", e);
            return "[unable to read request body]";
        }
    }
}
