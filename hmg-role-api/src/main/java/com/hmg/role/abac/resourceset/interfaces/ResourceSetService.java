package com.hmg.role.abac.resourceset.interfaces;

import com.hmg.role.abac.resourceset.dto.DeleteBulkResourceSetDto;
import com.hmg.role.abac.resourceset.dto.ResourceSetDto;
import com.hmg.role.abac.resourceset.dto.SearchResourceSetDto;
import com.hmg.role.abac.resourceset.dto.UpdateBulkResourceSetDto;
import com.hmg.role.abac.resourceset.dto.UpdateResourceSetDto;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.List;

public interface ResourceSetService {

    ResourceSetDto createResourceSet(ResourceSetDto resourceSetDto);

    ListResponseDto<ResourceSetDto> createBulkResourceSets(List<ResourceSetDto> resourceSetDtos);

    ListResponseDto<ResourceSetDto> getResourceSets(SearchResourceSetDto searchDto);

    ResourceSetDto getResourceSetByKey(String key);

    ResourceSetDto updateResourceSet(String key, UpdateResourceSetDto updateResourceSetDto);

    ListResponseDto<ResourceSetDto> updateBulkResourceSets(
            List<UpdateBulkResourceSetDto> updateBulkResourceSetDtos);

    void deleteResourceSet(String key);

    void deleteResourceSetCascade(String key);

    void deleteBulkResourceSets(DeleteBulkResourceSetDto deleteBulkResourceSetDto);

    void deleteBulkResourceSetsCascade(DeleteBulkResourceSetDto deleteBulkResourceSetDto);
}
