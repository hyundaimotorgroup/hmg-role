package com.hmg.role.rbac.resourcetype;

import com.hmg.role.admin.project.Project;
import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.resourceaction.ResourceAction;
import com.hmg.role.rbac.resourcetag.ResourceTag;
import com.hmg.role.rbac.resourcetype.dto.CreateResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.ResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.UpdateBulkResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.UpdateResourceTypeDto;
import com.hmg.role.sdk.common.util.Utils;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public abstract class ResourceTypeMapper {

    @Mapping(ignore = true, target = "id")
    @Mapping(source = "projectData", target = "project")
    @Mapping(source = "createResourceTypeDto.description", target = "description")
    @Mapping(source = "createResourceTypeDto.key", target = "key")
    @Mapping(source = "createResourceTypeDto.name", target = "name")
    public abstract ResourceType toResourceType(
            CreateResourceTypeDto createResourceTypeDto, Project projectData);

    public abstract void toResourceType(
            @MappingTarget ResourceType resourceType, UpdateResourceTypeDto updateResourceTypeDto);

    public abstract void toResourceType(
            @MappingTarget ResourceType resourceType,
            UpdateBulkResourceTypeDto updateBulkResourceTypeDto);

    @Mapping(source = "resourceType.description", target = "description")
    @Mapping(source = "resourceType.key", target = "key")
    @Mapping(expression = "java(toResourceActionNames(resourceActions))", target = "actions")
    @Mapping(source = "resourceType.parent.key", target = "parentKey")
    @Mapping(
            expression = "java(toIsoOffsetDateTime(resourceType.getCreatedAt()))",
            target = "createdAt")
    @Mapping(
            expression = "java(toIsoOffsetDateTime(resourceType.getUpdatedAt()))",
            target = "updatedAt")
    public abstract ResourceTypeDto toResourceTypeDto(
            ResourceType resourceType, List<ResourceAction> resourceActions, List<String> tags);

    protected List<String> toResourceActionNames(List<ResourceAction> resourceActions) {
        return resourceActions.stream().map(ResourceAction::getActionName).toList();
    }

    protected String toIsoOffsetDateTime(ZonedDateTime time) {
        if (time != null) {
            return Utils.formatToIso8601String(time);
        } else {
            return null;
        }
    }

    protected List<String> checkTag(String tag) {
        return !StringUtils.isBlank(tag) ? Arrays.asList(tag.split(",")) : null;
    }

    @Mapping(source = "resourceType.createdAt", target = "createdAt")
    @Mapping(source = "resourceType.updatedAt", target = "updatedAt")
    public ResourceTypeDto toResourceTypeDtoTree(ResourceType resourceType) {
        List<String> actions =
                resourceType.getResourceActions().stream()
                        .filter(action -> !action.isDeleted())
                        .map(ResourceAction::getActionName)
                        .collect(Collectors.toList());

        List<String> tags =
                resourceType.getResourceTags().stream()
                        .filter(tag -> !tag.isDeleted())
                        .map(ResourceTag::getTag)
                        .collect(Collectors.toList());

        List<ResourceTypeDto> children =
                resourceType.getChildren().stream()
                        .filter(child -> !child.isDeleted())
                        .map(this::toResourceTypeDtoTree)
                        .collect(Collectors.toList());

        var childrenCount = computeDeepCount(children);

        String parentKey = null;
        if (resourceType.getParent() != null) {
            parentKey = resourceType.getParent().getKey();
        }

        String createdAtStr = toIsoOffsetDateTime(resourceType.getCreatedAt());
        String updatedAtStr = toIsoOffsetDateTime(resourceType.getUpdatedAt());

        return ResourceTypeDto.builder()
                .description(resourceType.getDescription())
                .key(resourceType.getKey())
                .name(resourceType.getName())
                .actions(actions)
                .tags(tags)
                .children(children)
                .childrenCount(childrenCount)
                .parentKey(parentKey)
                .createdAt(createdAtStr)
                .updatedAt(updatedAtStr)
                .build();
    }

    private long computeDeepCount(List<ResourceTypeDto> children) {
        long total = 0;
        for (ResourceTypeDto child : children) {
            // 1 for the child itself, plus however many descendants it has
            total += 1 + computeDeepCount(child.children());
        }
        return total;
    }
}
