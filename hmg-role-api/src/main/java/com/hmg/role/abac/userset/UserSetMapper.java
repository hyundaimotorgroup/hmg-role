package com.hmg.role.abac.userset;

import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.abac.userset.dto.UpdateBulkUserSetDto;
import com.hmg.role.abac.userset.dto.UpdateUserSetDto;
import com.hmg.role.abac.userset.dto.UserSetConflictDetailDto;
import com.hmg.role.abac.userset.dto.UserSetConflictWithPolicyDto;
import com.hmg.role.abac.userset.dto.UserSetDto;
import com.hmg.role.admin.project.Project;
import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.sdk.common.util.Utils;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public abstract class UserSetMapper {

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "parents")
    @Mapping(ignore = true, target = "createdBy")
    @Mapping(ignore = true, target = "createdAt")
    @Mapping(ignore = true, target = "updatedBy")
    @Mapping(ignore = true, target = "updatedAt")
    @Mapping(expression = "java(false)", target = "deleted")
    @Mapping(source = "userSetDto.key", target = "key")
    @Mapping(source = "project", target = "project")
    @Mapping(source = "userSetDto.description", target = "description")
    @Mapping(source = "userSetDto.conditionGroupOperator", target = "conditionGroupOperator")
    @Mapping(source = "userSetDto.name", target = "name")
    public abstract UserSet toUserSet(UserSetDto userSetDto, Project project);

    @Mapping(source = "updateUserSetDto.description", target = "description")
    @Mapping(source = "updateUserSetDto.conditionGroupOperator", target = "conditionGroupOperator")
    public abstract void toUserSet(
            @MappingTarget UserSet userSet, UpdateUserSetDto updateUserSetDto);

    public abstract void toUserSet(
            @MappingTarget UserSet userSet, UpdateBulkUserSetDto updateUserSetDto);

    @Mapping(source = "conditionDtos", target = "conditionGroup")
    @Mapping(
            expression = "java(userSet.getParents().stream().map(UserSet::getKey).toList())",
            target = "parents")
    @Mapping(expression = "java(toIsoOffsetDateTime(userSet.getCreatedAt()))", target = "createdAt")
    @Mapping(source = "userSet.createdBy", target = "createdBy")
    @Mapping(expression = "java(toIsoOffsetDateTime(userSet.getUpdatedAt()))", target = "updatedAt")
    @Mapping(source = "userSet.updatedBy", target = "updatedBy")
    public abstract UserSetDto toUserSetDto(UserSet userSet, List<ConditionDto> conditionDtos);

    protected String toIsoOffsetDateTime(ZonedDateTime time) {
        return Utils.formatToIso8601String(time);
    }

    public abstract UserSetDto toUserSetDto(String key, UpdateUserSetDto userSetDto);

    public abstract UserSetDto toUserSetDto(
            String key, List<String> parents, UpdateUserSetDto userSetDto);

    public abstract UserSetConflictDetailDto toUserSetConflictDetail(
            Collection<UserSetConflictWithPolicyDto> userSets, Set<String> policyKeys);

    @Mapping(source = "updateBulkUserSetDto.key", target = "key")
    @Mapping(source = "updateBulkUserSetDto.description", target = "description")
    @Mapping(
            source = "updateBulkUserSetDto.conditionGroupOperator",
            target = "conditionGroupOperator")
    @Mapping(
            expression = "java(parents.stream().map(UserSet::getKey).toList())",
            target = "parents")
    public abstract UserSetDto toUserSetDto(
            UpdateBulkUserSetDto updateBulkUserSetDto, List<UserSet> parents);
}
