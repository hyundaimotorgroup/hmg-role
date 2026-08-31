package com.hmg.role.api.rbac;

import static com.hmg.role.util.Constants.MAX_500_SIZE;

import com.hmg.role.rbac.resourcetype.dto.CreateResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.DeleteBulkResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.ResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.ResourceTypePageRequestDto;
import com.hmg.role.rbac.resourcetype.dto.UpdateBulkResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.UpdateResourceTypeDto;
import com.hmg.role.rbac.resourcetype.interfaces.ResourceTypeService;
import com.hmg.role.rbac.resourcetype.projections.ResourceTypeParentProjection;
import com.hmg.role.util.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

@Tag(name = "Resource dataType API")
@RequiredArgsConstructor
@RequestMapping("/api/rbac/v1/resource-types")
@RestController
public class ResourceTypeController {
    private final ResourceTypeService resourceTypeService;

    // the purpose of using boolean multipleFlag is to show on swagger-ui dropdown selection
    @Operation(summary = "Create new Resource Type")
    @PostMapping
    @Parameter(name = "multiple", schema = @Schema(allowableValues = {"true", "false"}))
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceTypeDto createResourceType(
            @RequestBody @Valid CreateResourceTypeDto createResourceTypeDto) {
        return resourceTypeService.createResourceType(createResourceTypeDto);
    }

    // the purpose of using boolean multipleFlag is to show on swagger-ui dropdown selection
    @Operation(summary = "Bulk Create New Resource Types")
    @PostMapping(params = "multiple=true")
    @Parameter(
            name = "multiple",
            schema = @Schema(allowableValues = {"true", "false"}),
            required = true)
    @ResponseStatus(HttpStatus.CREATED)
    public ListResponseDto<ResourceTypeDto> createBulkResourceType(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_500_SIZE)
                    List<@Valid @NotNull CreateResourceTypeDto> createResourceTypeDtoList) {
        return resourceTypeService.createBulkResourceTypes(createResourceTypeDtoList);
    }

    @Operation(summary = "Get Available Parent Keys")
    @GetMapping("/keys/parent-keys")
    public ListResponseDto<ResourceTypeParentProjection> getAvailableParentKeys(
            @Parameter @RequestParam(required = false) String currentResourceTypeKey) {
        return resourceTypeService.getAvailableParentKeys(currentResourceTypeKey);
    }

    @Operation(summary = "List Resource Type")
    @GetMapping
    public ListResponseDto<ResourceTypeDto> listResourceTypes(
            @ParameterObject @ModelAttribute @Valid ResourceTypePageRequestDto pageRequestDto) {
        return resourceTypeService.listAllResourceTypes(pageRequestDto);
    }

    @Operation(summary = "Get Resource By Key")
    @GetMapping("/{resourceTypeKey}")
    public ResourceTypeDto getResourceTypeByResourceId(@PathVariable String resourceTypeKey) {
        String decodeResourceTypeKey = URLDecoder.decode(resourceTypeKey, StandardCharsets.UTF_8);
        return resourceTypeService.getResourceTypeByKey(decodeResourceTypeKey);
    }

    @Operation(summary = "Update Existing ResourceType By Key")
    @PutMapping("/{resourceTypeKey}")
    public ResourceTypeDto updateResourceType(
            @PathVariable String resourceTypeKey,
            @RequestBody @Valid UpdateResourceTypeDto updateResourceTypeDto) {
        String decodeResourceTypeKey = URLDecoder.decode(resourceTypeKey, StandardCharsets.UTF_8);
        return resourceTypeService.updateResourceType(updateResourceTypeDto, decodeResourceTypeKey);
    }

    @Operation(summary = "Bulk Update Existing Resource Types")
    @PutMapping
    public ListResponseDto<ResourceTypeDto> updateBulkResourceTypes(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_500_SIZE)
                    List<@Valid @NotNull UpdateBulkResourceTypeDto> updateBulkResourceTypeDtoList) {
        return resourceTypeService.updateBulkResourceTypes(updateBulkResourceTypeDtoList);
    }

    @Operation(summary = "Delete ResourceType By Key")
    @DeleteMapping("/{resourceTypeKey}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResourceType(
            @PathVariable String resourceTypeKey, @RequestParam(required = false) Boolean cascade) {
        String decodeResourceTypeKey = URLDecoder.decode(resourceTypeKey, StandardCharsets.UTF_8);

        if (Boolean.TRUE.equals(cascade)) {
            resourceTypeService.deleteCascadeResourceType(decodeResourceTypeKey);
        } else {
            resourceTypeService.deleteResourceType(decodeResourceTypeKey);
        }
    }

    @Operation(summary = "Bulk Delete Existing ResourceTypes")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBulkResourceTypes(
            @RequestBody @Valid DeleteBulkResourceTypeDto deleteBulkResourceTypeDto,
            @RequestParam(required = false) Boolean cascade) {

        if (Boolean.TRUE.equals(cascade)) {
            resourceTypeService.deleteCascadeBulkResourceTypes(deleteBulkResourceTypeDto);
        } else {
            resourceTypeService.deleteBulkResourceTypes(deleteBulkResourceTypeDto);
        }
    }
}
