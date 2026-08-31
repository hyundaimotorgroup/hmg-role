package com.hmg.role.rbac.template;

import com.hmg.role.abac.resourceset.action.ResourceSetActionRepository;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.template.dto.FilterTemplateRequestDto;
import com.hmg.role.rbac.template.dto.TemplateDto;
import com.hmg.role.rbac.template.interfaces.AbacTemplateService;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbacTemplateServiceImpl implements AbacTemplateService {

    private final ResourceSetActionRepository resourceSetActionRepository;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    @Override
    public ListResponseDto<TemplateDto> getTemplates(FilterTemplateRequestDto reqDto) {
        Project projectData = getCurrentProject();

        var pageRequest = reqDto.pageRequest();

        List<TemplateDto> combinedTemplatesDto =
                resourceSetActionRepository.findAllDistinctActionNames(projectData).stream()
                        .map(action -> new TemplateDto(action, "action"))
                        .toList();

        if (reqDto.getType() != null) {
            combinedTemplatesDto =
                    combinedTemplatesDto.stream()
                            .filter(dto -> dto.type().equalsIgnoreCase(reqDto.getType()))
                            .toList();
        }

        Page<TemplateDto> dtoPage =
                new PageImpl<>(combinedTemplatesDto, pageRequest, combinedTemplatesDto.size());

        return ListResponseDto.create(dtoPage);
    }

    private Project getCurrentProject() {
        return authorRequestScope.getProject();
    }
}
