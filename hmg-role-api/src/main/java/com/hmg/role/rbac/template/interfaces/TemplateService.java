package com.hmg.role.rbac.template.interfaces;

import com.hmg.role.rbac.template.dto.FilterTemplateRequestDto;
import com.hmg.role.rbac.template.dto.TemplateDto;
import com.hmg.role.util.dto.ListResponseDto;

public interface TemplateService {

    ListResponseDto<TemplateDto> getTemplates(FilterTemplateRequestDto filterTemplateDto);
}
