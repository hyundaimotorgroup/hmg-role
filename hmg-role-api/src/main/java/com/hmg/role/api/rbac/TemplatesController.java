package com.hmg.role.api.rbac;

import com.hmg.role.rbac.template.dto.FilterTemplateRequestDto;
import com.hmg.role.rbac.template.dto.TemplateDto;
import com.hmg.role.rbac.template.interfaces.TemplateService;
import com.hmg.role.util.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Templates")
@RequiredArgsConstructor
@RequestMapping("/api/rbac/v1/templates")
@RestController
public class TemplatesController {
    private final TemplateService templateService;

    @Operation(summary = "List Unique Templates")
    @GetMapping
    public ListResponseDto<TemplateDto> getTemplates(
            @ParameterObject @ModelAttribute @Valid
                    FilterTemplateRequestDto filterTemplateRequestDto) {
        return templateService.getTemplates(filterTemplateRequestDto);
    }
}
