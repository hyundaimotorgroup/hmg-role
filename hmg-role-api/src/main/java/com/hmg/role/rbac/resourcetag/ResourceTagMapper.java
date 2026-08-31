package com.hmg.role.rbac.resourcetag;

import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.resourcetype.ResourceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface ResourceTagMapper {

    @Mapping(source = "resourceType", target = "resourceType")
    @Mapping(ignore = true, target = "id")
    ResourceTag toResource(ResourceType resourceType, String tag);
}
