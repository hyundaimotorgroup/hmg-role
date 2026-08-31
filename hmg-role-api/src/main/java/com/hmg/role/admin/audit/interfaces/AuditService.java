package com.hmg.role.admin.audit.interfaces;

import com.hmg.role.admin.audit.dto.AuditFilterResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailDetailsRequestDto;
import com.hmg.role.admin.audit.dto.AuditTrailDetailsResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailsRequestDto;
import com.hmg.role.util.dto.ListResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuditService {
    void commitAsync(Object entityObject);

    ListResponseDto<AuditTrailResponseDto> getAuditTrails(
            HttpServletRequest request, AuditTrailsRequestDto filters);

    <T> AuditTrailDetailsResponseDto getAuditTrailDetails(
            AuditTrailDetailsRequestDto detailRequest);

    AuditFilterResponseDto getAuditTrailFilters();
}
