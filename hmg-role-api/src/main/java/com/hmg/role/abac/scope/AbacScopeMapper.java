package com.hmg.role.abac.scope;

import com.hmg.role.admin.project.Project;
import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.scope.dto.CreateScopeDto;
import com.hmg.role.rbac.scope.dto.ScopeConflictDetailDto;
import com.hmg.role.rbac.scope.dto.ScopeDto;
import com.hmg.role.rbac.scope.dto.UpdateScopeDto;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public abstract class AbacScopeMapper {

    @Mapping(target = "deleted", ignore = true)
    @Mapping(ignore = true, target = "id")
    @Mapping(source = "project", target = "project")
    @Mapping(source = "createDto.key", target = "key")
    @Mapping(source = "createDto.name", target = "name")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "updatedBy", target = "updatedBy")
    public abstract AbacScope toScope(
            CreateScopeDto createDto, Project project, String createdBy, String updatedBy);

    @Mapping(
            expression =
                    "java(finalScope.getName() == null ? finalScope.getKey() : finalScope.getName())",
            target = "name")
    public abstract ScopeDto toScopeDto(AbacScope finalScope);

    @Mapping(source = "updatedBy", target = "updatedBy")
    public abstract AbacScope toScope(
            @MappingTarget AbacScope scope, UpdateScopeDto updateScopeDto, String updatedBy);

    public abstract ScopeConflictDetailDto toDetailedUsageByUsersScopesPolicies(
            Set<String> userKeys, Set<String> policyKeys, Set<String> projectKeys);
}
