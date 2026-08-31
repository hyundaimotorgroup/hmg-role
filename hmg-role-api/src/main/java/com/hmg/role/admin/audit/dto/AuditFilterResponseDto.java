package com.hmg.role.admin.audit.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record AuditFilterResponseDto(List<String> keyTypes, List<String> paths) {}
