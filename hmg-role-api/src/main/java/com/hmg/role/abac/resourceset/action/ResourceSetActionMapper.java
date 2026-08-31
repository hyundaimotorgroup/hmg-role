package com.hmg.role.abac.resourceset.action;

import com.hmg.role.abac.resourceset.ResourceSet;
import com.hmg.role.common.config.CommonMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface ResourceSetActionMapper {

    @Mapping(source = "resourceSet", target = "resourceSet")
    @Mapping(source = "actionName", target = "actionName")
    @BeanMapping(ignoreByDefault = true)
    ResourceSetAction toResourceSetAction(ResourceSet resourceSet, String actionName);
}
