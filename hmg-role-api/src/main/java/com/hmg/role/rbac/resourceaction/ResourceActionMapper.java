package com.hmg.role.rbac.resourceaction;

import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.resourcetype.ResourceType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface ResourceActionMapper {

    @Mapping(source = "resourceType", target = "resourceType")
    @Mapping(source = "actionName", target = "actionName")
    @BeanMapping(ignoreByDefault = true)
    ResourceAction toResourceAction(ResourceType resourceType, String actionName);
}
