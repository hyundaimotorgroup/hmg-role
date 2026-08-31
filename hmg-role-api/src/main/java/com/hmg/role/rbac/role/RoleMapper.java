package com.hmg.role.rbac.role;

import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.role.dto.CreateRoleDto;
import com.hmg.role.rbac.role.dto.RoleConflictDetailDto;
import com.hmg.role.rbac.role.dto.RoleDto;
import com.hmg.role.rbac.role.dto.RoleWithUserCountDto;
import com.hmg.role.rbac.role.dto.UpdateBulkRoleDto;
import com.hmg.role.rbac.role.dto.UpdateRoleDto;
import com.hmg.role.rbac.role.dto.UserConflictWithRoleDto;
import com.hmg.role.sdk.common.util.Utils;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public interface RoleMapper {

    @Mapping(source = "createRoleDto.key", target = "key")
    Role toRole(CreateRoleDto createRoleDto);

    void toRole(@MappingTarget Role role, UpdateRoleDto updateRoleDto);

    void toRole(@MappingTarget Role role, UpdateBulkRoleDto updateBulkRoleDto);

    @Mapping(source = "role.key", target = "key")
    @Mapping(expression = "java(toIsoOffsetDateTime(role.getCreatedAt()))", target = "createdAt")
    @Mapping(source = "role.createdBy", target = "createdBy")
    @Mapping(expression = "java(toIsoOffsetDateTime(role.getUpdatedAt()))", target = "updatedAt")
    @Mapping(source = "role.updatedBy", target = "updatedBy")
    RoleDto toRoleDto(Role role);

    default String toIsoOffsetDateTime(ZonedDateTime time) {
        return Utils.formatToIso8601String(time);
    }

    RoleWithUserCountDto toRoleWithUserCountDto(Role role, long userCount);

    RoleConflictDetailDto toDetailedUsageByUsersPolicies(
            Collection<UserConflictWithRoleDto> users, Set<String> policyKeys);

    default RoleConflictDetailDto toDetailedUsageByUsers(
            Collection<UserConflictWithRoleDto> users) {
        return RoleConflictDetailDto.builder().users(users).build();
    }

    default RoleConflictDetailDto toDetailedUsageByPolicies(Set<String> policyKeys) {
        return RoleConflictDetailDto.builder().policyKeys(policyKeys).build();
    }
}
