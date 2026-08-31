package com.hmg.role.rbac.user;

import com.hmg.role.admin.project.Project;
import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.user.dto.CreateUserDto;
import com.hmg.role.rbac.user.dto.UpdateBulkUserDto;
import com.hmg.role.rbac.user.dto.UserDto;
import com.hmg.role.rbac.userscoperole.dto.UserScopeRoleDto;
import com.hmg.role.sdk.common.util.Utils;
import java.time.ZonedDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public interface UserMapper {

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "createdAt")
    @Mapping(ignore = true, target = "createdBy")
    @Mapping(ignore = true, target = "updatedAt")
    @Mapping(ignore = true, target = "updatedBy")
    @Mapping(source = "createUserDto.key", target = "userKey")
    @Mapping(source = "createUserDto.name", target = "name")
    @Mapping(target = "scopedRoles", ignore = true)
    @Mapping(source = "project", target = "project")
    @Mapping(source = "createUserDto.metadata", target = "metadata")
    User toUser(CreateUserDto createUserDto, Project project);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "createdAt")
    @Mapping(ignore = true, target = "createdBy")
    @Mapping(ignore = true, target = "updatedAt")
    @Mapping(ignore = true, target = "updatedBy")
    @Mapping(source = "updateBulkUserDto.name", target = "name")
    void toUser(@MappingTarget User user, UpdateBulkUserDto updateBulkUserDto);

    @Mapping(source = "user.userKey", target = "key")
    @Mapping(source = "userScopeRoleDto", target = "scopeRoles")
    @Mapping(expression = "java(toIsoOffsetDateTime(user.getCreatedAt()))", target = "createdAt")
    @Mapping(source = "user.createdBy", target = "createdBy")
    @Mapping(expression = "java(toIsoOffsetDateTime(user.getUpdatedAt()))", target = "updatedAt")
    @Mapping(source = "user.updatedBy", target = "updatedBy")
    UserDto toUserDto(User user, List<UserScopeRoleDto> userScopeRoleDto);

    default String toIsoOffsetDateTime(ZonedDateTime time) {
        return Utils.formatToIso8601String(time);
    }
}
