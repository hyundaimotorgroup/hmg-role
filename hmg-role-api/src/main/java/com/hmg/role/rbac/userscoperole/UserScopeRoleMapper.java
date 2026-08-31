package com.hmg.role.rbac.userscoperole;

import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.user.User;
import com.hmg.role.rbac.userscoperole.dto.CreateUserScopeRoleDto;
import com.hmg.role.rbac.userscoperole.dto.UpdateUserScopeRoleDto;
import com.hmg.role.rbac.userscoperole.dto.UserScopeRoleDto;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface UserScopeRoleMapper {
    @Mapping(source = "role.key", target = "roleKey")
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "scope.key", target = "scopeKey")
    @Mapping(source = "scope.name", target = "scopeName")
    UserScopeRoleDto toDto(UserScopeRole userScopeRole);

    default List<UserScopeRole> toCreateEntities(
            List<CreateUserScopeRoleDto> dtos,
            User user,
            Map<String, Role> roleMap,
            Map<String, Scope> scopeMap) {
        return dtos.stream().map(dto -> toCreateEntity(dto, user, roleMap, scopeMap)).toList();
    }

    default UserScopeRole toCreateEntity(
            CreateUserScopeRoleDto dto,
            User user,
            Map<String, Role> roleMap,
            Map<String, Scope> scopeMap) {
        UserScopeRole scopedRole = new UserScopeRole();
        scopedRole.setUser(user);
        scopedRole.setRole(roleMap.get(dto.roleKey()));
        scopedRole.setScope(scopeMap.get(dto.scopeKey()));
        scopedRole.setDeleted(false);
        return scopedRole;
    }

    default List<UserScopeRole> toUpdateEntities(
            List<UpdateUserScopeRoleDto> dtos,
            User user,
            Map<String, Role> roleMap,
            Map<String, Scope> scopeMap) {
        return dtos.stream().map(dto -> toUpdateEntity(dto, user, roleMap, scopeMap)).toList();
    }

    default UserScopeRole toUpdateEntity(
            UpdateUserScopeRoleDto dto,
            User user,
            Map<String, Role> roleMap,
            Map<String, Scope> scopeMap) {
        UserScopeRole scopedRole = new UserScopeRole();
        scopedRole.setUser(user);
        scopedRole.setRole(roleMap.get(dto.roleKey()));
        scopedRole.setScope(scopeMap.get(dto.scopeKey()));
        scopedRole.setDeleted(false);
        return scopedRole;
    }
}
