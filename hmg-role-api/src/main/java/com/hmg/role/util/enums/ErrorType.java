package com.hmg.role.util.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Deprecated
@Getter
@RequiredArgsConstructor
public enum ErrorType {
    INVALID_PARAMETERS(HttpStatus.BAD_REQUEST, "check the parameters", "INVALID_PARAMETERS"),
    PROJECT_NOT_FOUND(HttpStatus.BAD_REQUEST, "project not found", "PROJECT_NOT_FOUND"),
    PROJECT_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "project already exist", "PROJECT_ALREADY_EXIST"),
    RESOURCE_TYPE_ALREADY_EXIST(
            HttpStatus.BAD_REQUEST,
            "resource dataType already exist",
            "RESOURCE_TYPE_ALREADY_EXIST"),
    POLICY_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "policy already exist", "POLICY_ALREADY_EXIST"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized", "UNAUTHORIZED");
    private final HttpStatus httpStatusType;
    private final String message;
    private final String code;
}
