package com.hmg.role.api.rbac;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.rbac.role.dto.CreateRoleDto;
import com.hmg.role.rbac.role.dto.DeleteBulkRoleDto;
import com.hmg.role.rbac.role.dto.RoleDto;
import com.hmg.role.rbac.role.dto.RoleScopeUserRequestDto;
import com.hmg.role.rbac.role.dto.RoleSearchRequestDto;
import com.hmg.role.rbac.role.dto.RoleWithUserCountDto;
import com.hmg.role.rbac.role.dto.UpdateBulkRoleDto;
import com.hmg.role.rbac.role.dto.UpdateRoleDto;
import com.hmg.role.rbac.role.interfaces.RoleService;
import com.hmg.role.rbac.userscoperole.dto.ScopeUserDto;
import com.hmg.role.util.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RBAC Role Management")
@RequiredArgsConstructor
@RequestMapping("/api/rbac/v1/roles")
@RestController
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "create role")
    @PostMapping
    @Parameter(name = "multiple", schema = @Schema(allowableValues = {"true", "false"}))
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDto createRole(@RequestBody @Valid CreateRoleDto createRoleDto) {
        return roleService.createRole(createRoleDto);
    }

    // multipleFlag are used for calling API via swagger
    @Operation(summary = "Bulk Create New Roles")
    @PostMapping(params = "multiple=true")
    @Parameter(
            name = "multiple",
            schema = @Schema(allowableValues = {"true", "false"}),
            required = true)
    @ResponseStatus(HttpStatus.CREATED)
    public ListResponseDto<RoleDto> createBulkRoles(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull CreateRoleDto> createRoleDtoList) {
        return roleService.createBulkRoles(createRoleDtoList);
    }

    @Operation(summary = "list users mapped to a role, optionally filtered by scope or keyword")
    @GetMapping("/{key}/scopeUsers")
    public ListResponseDto<ScopeUserDto> listScopeUsersByRole(
            @PathVariable String key,
            @ParameterObject @ModelAttribute @Valid RoleScopeUserRequestDto roleScopeUserRequest) {
        return roleService.listScopeUsersByRole(key, roleScopeUserRequest);
    }

    @Operation(summary = "list role data")
    @GetMapping
    public ListResponseDto<RoleWithUserCountDto> listRole(
            @ParameterObject @ModelAttribute @Valid RoleSearchRequestDto paginationDto) {
        return roleService.listRole(paginationDto);
    }

    @Operation(summary = "get role by key")
    @GetMapping("/{key}")
    public RoleDto getRoleByKey(@PathVariable String key) {
        return roleService.getRoleByKey(key);
    }

    @Operation(summary = "update role")
    @PutMapping("/{key}")
    public RoleDto updateRole(
            @PathVariable String key, @RequestBody @Valid UpdateRoleDto updateRoleDto) {
        return roleService.updateRole(key, updateRoleDto);
    }

    @Operation(summary = "Bulk Update Existing Roles")
    @PutMapping
    public ListResponseDto<RoleDto> updateBulkRoles(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull UpdateBulkRoleDto> updateRoleDtoList) {
        return roleService.updateBulkRoles(updateRoleDtoList);
    }

    @Operation(summary = "delete role")
    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(
            @PathVariable String key, @RequestParam(required = false) Boolean cascade) {
        if (Boolean.TRUE.equals(cascade)) {
            roleService.deleteRoleCascade(key);
        } else {
            roleService.deleteRole(key);
        }
    }

    @Operation(summary = "Bulk Delete Existing Roles")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBulkRoles(
            @RequestBody @Valid DeleteBulkRoleDto deleteBulkRoleDto,
            @RequestParam(required = false) Boolean cascade) {
        if (Boolean.TRUE.equals(cascade)) {
            roleService.deleteBulkRolesCascade(deleteBulkRoleDto);
        } else {
            roleService.deleteBulkRoles(deleteBulkRoleDto);
        }
    }
}
