package com.hmg.role.rbac.role.interfaces;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.role.dto.CreateRoleDto;
import com.hmg.role.rbac.role.dto.DeleteBulkRoleDto;
import com.hmg.role.rbac.role.dto.RoleDto;
import com.hmg.role.rbac.role.dto.RoleScopeUserRequestDto;
import com.hmg.role.rbac.role.dto.RoleSearchRequestDto;
import com.hmg.role.rbac.role.dto.RoleWithUserCountDto;
import com.hmg.role.rbac.role.dto.UpdateBulkRoleDto;
import com.hmg.role.rbac.role.dto.UpdateRoleDto;
import com.hmg.role.rbac.userscoperole.dto.ScopeUserDto;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.Collection;
import java.util.List;

public interface RoleService {

    RoleDto createRole(CreateRoleDto createRoleDto);

    ListResponseDto<RoleDto> createBulkRoles(List<CreateRoleDto> createRoleDtos);

    ListResponseDto<RoleWithUserCountDto> listRole(RoleSearchRequestDto paginationDto);

    RoleDto getRoleByKey(String roleKey);

    List<Role> findRolesAndThrowIfNotExists(Collection<String> roleKeys);

    RoleDto updateRole(String key, UpdateRoleDto updateRoleDto);

    ListResponseDto<RoleDto> updateBulkRoles(List<UpdateBulkRoleDto> updateBulkRoleDtos);

    void deleteRole(String roleKey);

    void deleteRoleCascade(String key);

    void deleteBulkRoles(DeleteBulkRoleDto deleteBulkRoleDto);

    void deleteBulkRolesCascade(DeleteBulkRoleDto deleteBulkRoleDto);

    List<Role> findByKeyInAndProjectAndDeletedFalse(List<String> roleKeys, Project project);

    ListResponseDto<ScopeUserDto> listScopeUsersByRole(String roleKey, RoleScopeUserRequestDto req);
}
