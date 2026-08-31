package com.hmg.role.api.abac;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.abac.resourceset.dto.DeleteBulkResourceSetDto;
import com.hmg.role.abac.resourceset.dto.ResourceSetDto;
import com.hmg.role.abac.resourceset.dto.SearchResourceSetDto;
import com.hmg.role.abac.resourceset.dto.UpdateBulkResourceSetDto;
import com.hmg.role.abac.resourceset.dto.UpdateResourceSetDto;
import com.hmg.role.abac.resourceset.interfaces.ResourceSetService;
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

@Tag(name = "ResourceSet")
@RequiredArgsConstructor
@RequestMapping("/api/abac/v1/resource-sets")
@RestController
public class ResourceSetController {

    private final ResourceSetService resourceSetService;

    @Operation(summary = "Create new resource set")
    @PostMapping(params = "multiple=false")
    @Parameter(
            name = "multiple",
            schema = @Schema(allowableValues = {"true", "false"}),
            required = true)
    public ResourceSetDto create(@RequestBody @Valid ResourceSetDto resourceSetDto) {
        return resourceSetService.createResourceSet(resourceSetDto);
    }

    @Operation(summary = "Create Bulk New Resource Sets")
    @PostMapping(params = "multiple=true")
    @Parameter(
            name = "multiple",
            schema = @Schema(allowableValues = {"true", "false"}),
            required = true)
    public ListResponseDto<ResourceSetDto> createBulk(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull ResourceSetDto> resourceSetDtos) {
        return resourceSetService.createBulkResourceSets(resourceSetDtos);
    }

    @Operation(summary = "list of resource set")
    @GetMapping
    public ListResponseDto<ResourceSetDto> list(
            @ParameterObject @ModelAttribute @Valid SearchResourceSetDto dto) {
        return resourceSetService.getResourceSets(dto);
    }

    @Operation(summary = "Get of resource set by key")
    @GetMapping("/{key}")
    public ResourceSetDto getByKey(@PathVariable String key) {
        return resourceSetService.getResourceSetByKey(key);
    }

    @Operation(summary = "Update existing resource set data")
    @PutMapping("/{key}")
    public ResourceSetDto update(
            @PathVariable String key,
            @RequestBody @Valid UpdateResourceSetDto updateResourceSetDto) {
        return resourceSetService.updateResourceSet(key, updateResourceSetDto);
    }

    @Operation(summary = "Update Bulk Existing Resource Sets")
    @PutMapping
    public ListResponseDto<ResourceSetDto> updateBulk(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull UpdateBulkResourceSetDto> updateBulkResourceSetDtoList) {
        return resourceSetService.updateBulkResourceSets(updateBulkResourceSetDtoList);
    }

    @Operation(summary = "delete existing resource set data")
    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "false") Boolean cascade) {

        if (Boolean.TRUE.equals(cascade)) {
            resourceSetService.deleteResourceSetCascade(key);
        } else {
            resourceSetService.deleteResourceSet(key);
        }
    }

    @Operation(summary = "Delete Bulk Existing Resource Sets")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBulk(
            @RequestBody @Valid DeleteBulkResourceSetDto deleteBulkResourceSetDto,
            @RequestParam(required = false, defaultValue = "false") Boolean cascade) {

        if (Boolean.TRUE.equals(cascade)) {
            resourceSetService.deleteBulkResourceSetsCascade(deleteBulkResourceSetDto);
        } else {
            resourceSetService.deleteBulkResourceSets(deleteBulkResourceSetDto);
        }
    }
}
