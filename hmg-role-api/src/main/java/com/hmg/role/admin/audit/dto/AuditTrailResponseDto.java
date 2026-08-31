package com.hmg.role.admin.audit.dto;

import lombok.Builder;

@Builder
public record AuditTrailResponseDto(
        String commitId,
        String key,
        String entityPath,
        String requestPath,
        String requestMethod,
        String author,
        String ip,
        String eventTimestamp,
        String requestId,
        String userClassDesignator) {}
