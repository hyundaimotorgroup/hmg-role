package com.hmg.role.admin.audit.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Builder;

@Builder
public record AuditTrailDetailsResponseDto(
        @JsonRawValue
                // indicate to Spring/Jackson not to escape this
                String snapshot) {}
