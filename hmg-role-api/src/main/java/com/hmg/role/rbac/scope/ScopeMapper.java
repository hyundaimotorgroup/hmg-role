package com.hmg.role.rbac.scope;

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
public interface ScopeMapper {

    @Mapping(target = "deleted", ignore = true)
    @Mapping(ignore = true, target = "scopeId")
    @Mapping(source = "createDto.key", target = "key")
    @Mapping(source = "createDto.name", target = "name")
    Scope toScope(CreateScopeDto createDto, Project project);

    @Mapping(
            expression =
                    "java(finalScope.getName() == null ? finalScope.getKey() : finalScope.getName())",
            target = "name")
    ScopeDto toScopeDto(Scope finalScope);

    Scope toScope(@MappingTarget Scope scope, UpdateScopeDto updateScopeDto);

    ScopeConflictDetailDto toDetailedUsageByUsersScopesPolicies(
            Set<String> userKeys, Set<String> policyKeys, Set<String> projectKeys);
}
