package com.hmg.role.api.abac;

import com.hmg.role.abac.permission.dto.AbacPermissionFlattenedResponseDto;
import com.hmg.role.abac.permission.dto.AbacPermissionRequestDto;
import com.hmg.role.abac.permission.dto.AbacPermissionResponseDto;
import com.hmg.role.abac.permission.interfaces.AbacPermissionService;
import com.hmg.role.util.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ABAC Permission")
@RequiredArgsConstructor
@RequestMapping("/api/abac/v1/permissions")
@RestController
public class AbacPermissionController {

    private final AbacPermissionService abacPermissionService;

    // flag variable are used only for swagger ui
    @Operation(summary = "Checking ABAC permission for sets of resources")
    @PostMapping
    public ListResponseDto<AbacPermissionResponseDto> checkingAbacPermissions(
            @Parameter(name = "flattenResponseFormat") Boolean flag,
            @RequestBody @Valid AbacPermissionRequestDto request) {
        return abacPermissionService.getAllPermissions(request);
    }

    // flag variable are used only for swagger ui
    @Operation(
            summary =
                    "Checking ABAC permission for sets of resources with flattened Response Format")
    @PostMapping(params = "flattenResponseFormat=true")
    public ListResponseDto<AbacPermissionFlattenedResponseDto>
            checkingAbacPermissionsWithFlattenResponseFormat(
                    @Parameter(name = "flattenResponseFormat", required = true) Boolean flag,
                    @RequestBody @Valid AbacPermissionRequestDto request) {
        return abacPermissionService.getAllPermissionsFlattened(request);
    }
}
