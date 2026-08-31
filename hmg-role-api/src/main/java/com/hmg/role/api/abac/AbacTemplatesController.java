package com.hmg.role.api.abac;

import com.hmg.role.rbac.template.dto.FilterTemplateRequestDto;
import com.hmg.role.rbac.template.dto.TemplateDto;
import com.hmg.role.rbac.template.interfaces.AbacTemplateService;
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

@Tag(name = "ABAC Templates")
@RequiredArgsConstructor
@RequestMapping("/api/abac/v1/templates")
@RestController
public class AbacTemplatesController {

    private final AbacTemplateService templateService;

    @Operation(summary = "List Unique Templates")
    @GetMapping
    public ListResponseDto<TemplateDto> getTemplates(
            @ParameterObject @ModelAttribute @Valid
                    FilterTemplateRequestDto filterTemplateRequestDto) {
        return templateService.getTemplates(filterTemplateRequestDto);
    }
}
