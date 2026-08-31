package com.hmg.role.rbac.resourcetype.interfaces;

import com.hmg.role.rbac.resourcetype.dto.CreateResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.DeleteBulkResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.ResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.ResourceTypePageRequestDto;
import com.hmg.role.rbac.resourcetype.dto.UpdateBulkResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.UpdateResourceTypeDto;
import com.hmg.role.rbac.resourcetype.projections.ResourceTypeParentProjection;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.List;

public interface ResourceTypeService {
    ListResponseDto<ResourceTypeDto> listAllResourceTypes(
            ResourceTypePageRequestDto pageRequestDto);

    ResourceTypeDto getResourceTypeByKey(String key);

    ResourceTypeDto createResourceType(CreateResourceTypeDto createResourceTypeDto);

    ListResponseDto<ResourceTypeDto> createBulkResourceTypes(
            List<CreateResourceTypeDto> createResourceTypeDtos);

    ResourceTypeDto updateResourceType(
            UpdateResourceTypeDto updateResourceTypeDto, String resourceKey);

    ListResponseDto<ResourceTypeDto> updateBulkResourceTypes(
            List<UpdateBulkResourceTypeDto> updateBulkResourceTypeDtos);

    ListResponseDto<ResourceTypeParentProjection> getAvailableParentKeys(
            String currentResourceTypeKey);

    void deleteResourceType(String resourceKey);

    void deleteCascadeResourceType(String resourceKey);

    void deleteBulkResourceTypes(DeleteBulkResourceTypeDto deleteBulkResourceTypeDto);

    void deleteCascadeBulkResourceTypes(DeleteBulkResourceTypeDto deleteBulkResourceTypeDto);
}
