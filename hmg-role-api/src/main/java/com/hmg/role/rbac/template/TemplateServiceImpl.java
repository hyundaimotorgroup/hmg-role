package com.hmg.role.rbac.template;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.resourcetype.ResourceTypeRepository;
import com.hmg.role.rbac.template.dto.FilterTemplateRequestDto;
import com.hmg.role.rbac.template.dto.TemplateDto;
import com.hmg.role.rbac.template.interfaces.TemplateService;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final ResourceTypeRepository resourceTypeRepository;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    public ListResponseDto<TemplateDto> getTemplates(FilterTemplateRequestDto filterTemplateDto) {
        Project projectData = getCurrentProject();

        var pageRequest = PageRequest.of(filterTemplateDto.getPage(), filterTemplateDto.getSize());

        List<TemplateDto> resourceActions =
                resourceTypeRepository.findAllResourceActionNames(projectData).stream()
                        .map(action -> new TemplateDto(action, "action"))
                        .toList();

        List<TemplateDto> resourceTags =
                resourceTypeRepository.findAllResourceTagNames(projectData).stream()
                        .map(tag -> new TemplateDto(tag, "tag"))
                        .toList();

        List<TemplateDto> combinedTemplatesDto =
                Stream.concat(resourceActions.stream(), resourceTags.stream()).toList();

        if (filterTemplateDto.getType() != null) {
            combinedTemplatesDto =
                    combinedTemplatesDto.stream()
                            .filter(dto -> dto.type().equalsIgnoreCase(filterTemplateDto.getType()))
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
