package com.hmg.role.api.rbac;

import com.hmg.role.rbac.permission.dto.PermissionFlattenedResponseDto;
import com.hmg.role.rbac.permission.dto.PermissionRequestDto;
import com.hmg.role.rbac.permission.dto.PermissionResponseDto;
import com.hmg.role.rbac.permission.interfaces.PermissionService;
import com.hmg.role.util.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Permission")
@RequiredArgsConstructor
@RequestMapping("/api/rbac/v1/permissions")
@RestController
public class PermissionController {

    @Autowired private PermissionService permissionService;

    @Operation(summary = "Checking permission for sets of resources")
    @Parameter(
            name = "flattenResponseFormat",
            schema = @Schema(allowableValues = {"true", "false"}))
    @PostMapping
    public ListResponseDto<PermissionResponseDto> checkingPermissions(
            @RequestBody @Valid PermissionRequestDto request) {
        return permissionService.getAllPermissions(request);
    }

    @Operation(summary = "Checking permission for sets of resources with flattened Response Format")
    @Parameter(
            name = "flattenResponseFormat",
            schema =
                    @Schema(
                            allowableValues = {"true", "false"},
                            required = true))
    @PostMapping(params = "flattenResponseFormat=true")
    public ListResponseDto<PermissionFlattenedResponseDto>
            checkingPermissionsWithFlattenResponseFormat(
                    @RequestBody @Valid PermissionRequestDto request) {
        return permissionService.getAllPermissionsFlattened(request);
    }
}
