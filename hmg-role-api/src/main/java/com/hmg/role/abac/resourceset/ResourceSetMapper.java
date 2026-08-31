package com.hmg.role.abac.resourceset;

import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.abac.logicalexpression.dto.OperandDto;
import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.resourceset.dto.ResourceSetDto;
import com.hmg.role.abac.resourceset.dto.UpdateBulkResourceSetDto;
import com.hmg.role.abac.resourceset.dto.UpdateResourceSetDto;
import com.hmg.role.abac.resourceset.operand.ResourceSetOperand;
import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.sdk.common.util.Utils;
import com.hmg.role.util.enums.ConditionOperator;
import java.time.ZonedDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public abstract class ResourceSetMapper {

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "actions")
    @Mapping(ignore = true, target = "conditionGroup")
    @Mapping(ignore = true, target = "createdBy")
    @Mapping(ignore = true, target = "createdAt")
    @Mapping(ignore = true, target = "updatedBy")
    @Mapping(ignore = true, target = "updatedAt")
    @Mapping(source = "dto.key", target = "key")
    @Mapping(source = "parent", target = "parent")
    @Mapping(source = "dto.name", target = "name")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.conditionGroupOperator", target = "conditionGroupOperator")
    public abstract ResourceSet toResourceSet(ResourceSetDto dto, ResourceSet parent);

    @Mapping(ignore = true, target = "actions")
    public abstract void toResourceSet(
            @MappingTarget ResourceSet resourceSet, UpdateResourceSetDto updateResourceSetDto);

    @Mapping(ignore = true, target = "key")
    @Mapping(ignore = true, target = "actions")
    @Mapping(ignore = true, target = "conditionGroup")
    @Mapping(ignore = true, target = "parent")
    public abstract void toResourceSet(
            @MappingTarget ResourceSet resourceSet,
            UpdateBulkResourceSetDto updateBulkResourceSetDto);

    public abstract ResourceSetDto toResourceSetDto(
            String key, UpdateResourceSetDto updateResourceSetDto, String parent);

    @Mapping(source = "conditionSetDtos", target = "conditionGroup")
    @Mapping(expression = "java(actionToString(resourceSet.getActions()))", target = "actions")
    @Mapping(
            expression =
                    "java(resourceSet.getParent() != null ? resourceSet.getParent().getKey() : null)",
            target = "parent")
    @Mapping(
            expression = "java(toIsoOffsetDateTime(resourceSet.getCreatedAt()))",
            target = "createdAt")
    @Mapping(source = "resourceSet.createdBy", target = "createdBy")
    @Mapping(
            expression = "java(toIsoOffsetDateTime(resourceSet.getUpdatedAt()))",
            target = "updatedAt")
    @Mapping(source = "resourceSet.updatedBy", target = "updatedBy")
    public abstract ResourceSetDto toResourceSetDto(
            ResourceSet resourceSet, List<ConditionDto> conditionSetDtos);

    public abstract ResourceSetDto toResourceSetDto(
            UpdateBulkResourceSetDto updateBulkResourceSetDto, String parent);

    @Mapping(target = "type", source = "entity.conditionOperand.type")
    @Mapping(target = "dataType", source = "entity.conditionOperand.dataType")
    public abstract OperandDto toOperandDto(ResourceSetOperand entity);

    public abstract ConditionDto toConditionDto(
            OperandDto left, ConditionOperator operator, OperandDto right);

    public List<String> actionToString(List<ResourceSetAction> resourceSetActions) {
        return resourceSetActions.stream().map(ResourceSetAction::getActionName).toList();
    }

    protected String toIsoOffsetDateTime(ZonedDateTime time) {
        return Utils.formatToIso8601String(time);
    }
}
