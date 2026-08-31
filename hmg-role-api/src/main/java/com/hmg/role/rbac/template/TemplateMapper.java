package com.hmg.role.rbac.template;

import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.template.dto.TemplateDto;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public interface TemplateMapper {

    TemplateDto toTemplateDto(String action, String type);
}
