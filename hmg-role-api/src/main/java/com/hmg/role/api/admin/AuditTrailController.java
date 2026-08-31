package com.hmg.role.api.admin;

import com.hmg.role.admin.audit.dto.AuditFilterResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailDetailsRequestDto;
import com.hmg.role.admin.audit.dto.AuditTrailDetailsResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailsRequestDto;
import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.util.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tags({@Tag(name = "AuditTrail")})
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/audit-trails")
@RestController
public class AuditTrailController {
    private final AuditService auditService;

    @Operation(summary = "Get audit log")
    @GetMapping
    public ListResponseDto<AuditTrailResponseDto> getAuditTrail(
            HttpServletRequest request,
            @ParameterObject @ModelAttribute @Valid AuditTrailsRequestDto requestDto) {
        return auditService.getAuditTrails(request, requestDto);
    }

    @Operation(summary = "Get audit log details")
    @GetMapping(path = "/detail")
    public AuditTrailDetailsResponseDto getAuditTrailDetails(
            @ParameterObject @ModelAttribute @Valid AuditTrailDetailsRequestDto requestDto) {
        return auditService.getAuditTrailDetails(requestDto);
    }

    @Operation(summary = "Get audit filters")
    @GetMapping(path = "/filters")
    public AuditFilterResponseDto getAuditFilters() {
        return auditService.getAuditTrailFilters();
    }
}
