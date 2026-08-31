package com.hmg.role.util.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GlobalErrorResponseDto {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Object detail;
}
