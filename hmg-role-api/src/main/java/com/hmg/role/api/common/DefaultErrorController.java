package com.hmg.role.api.common;

import com.hmg.role.util.Constants;
import com.hmg.role.util.dto.GlobalErrorResponseDto;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DefaultErrorController implements ErrorController {

    private static final String LOG_MESSAGE_FORMAT =
            "HTTP {} error: {}, "
                    + "traceId: {}, clientIp: {}, projectKey: {}, memberKey: {}, apiKey: {}, "
                    + "message: {}, errors: {}, errorAttributes: {}";

    private final ErrorAttributes errorAttributes;

    @RequestMapping("/error") // NOSONAR: this is intended to catch general errors
    public ResponseEntity<GlobalErrorResponseDto> handleError(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);
        ErrorAttributeOptions attrs =
                ErrorAttributeOptions.of(
                        ErrorAttributeOptions.Include.EXCEPTION,
                        ErrorAttributeOptions.Include.MESSAGE,
                        ErrorAttributeOptions.Include.BINDING_ERRORS);

        Map<String, Object> attributes = errorAttributes.getErrorAttributes(webRequest, attrs);

        // Get the exception if available
        Throwable exception =
                Optional.ofNullable(
                                (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION))
                        .orElse(errorAttributes.getError(webRequest));
        // Get status from request attribute (more reliable than error attributes)
        Integer status =
                Optional.ofNullable(
                                (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
                        .orElse((Integer) attributes.get("status"));
        // Get the request URI where the error occurred
        String path =
                Optional.ofNullable(
                                (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI))
                        .orElse(request.getRequestURI());
        String message = (String) attributes.get("message");
        // String error = ((String) attributes.get("error"));
        Object errors = attributes.get("errors");

        Optional<Integer> statusNullable = Optional.ofNullable(status);
        HttpStatus httpStatus =
                statusNullable.map(HttpStatus::valueOf).orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        String errorReason = httpStatus.getReasonPhrase();

        // Log the error
        boolean is5xxError = statusNullable.map(s -> s >= 500).orElse(true);
        if (is5xxError) {
            log.error(
                    LOG_MESSAGE_FORMAT,
                    status,
                    path,
                    MDC.get(Constants.MDC_KEY_TRACE_ID),
                    MDC.get(Constants.MDC_KEY_USER_IP),
                    MDC.get(Constants.MDC_KEY_PROJECT_KEY),
                    MDC.get(Constants.MDC_KEY_MEMBER_KEY),
                    MDC.get(Constants.MDC_KEY_API_KEY),
                    message,
                    errors,
                    attributes,
                    exception);
        } else {
            log.warn(
                    LOG_MESSAGE_FORMAT,
                    status,
                    path,
                    MDC.get(Constants.MDC_KEY_TRACE_ID),
                    MDC.get(Constants.MDC_KEY_USER_IP),
                    MDC.get(Constants.MDC_KEY_PROJECT_KEY),
                    MDC.get(Constants.MDC_KEY_MEMBER_KEY),
                    MDC.get(Constants.MDC_KEY_API_KEY),
                    message,
                    errors,
                    attributes,
                    exception);
        }

        GlobalErrorResponseDto.GlobalErrorResponseDtoBuilder bodyBuilder = // haha get it
                GlobalErrorResponseDto.builder()
                        .timestamp(
                                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .status(httpStatus.value())
                        .path(path);

        if (!httpStatus.is5xxServerError()) {
            // tell clients what are their mistakes
            bodyBuilder = bodyBuilder.message(message);
        } else {
            // but hide our mistakes
            bodyBuilder = bodyBuilder.error(errorReason);
        }

        return new ResponseEntity<>(bodyBuilder.build(), httpStatus);
    }
}
