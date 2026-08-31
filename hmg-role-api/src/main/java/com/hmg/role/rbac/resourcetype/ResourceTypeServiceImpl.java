package com.hmg.role.rbac.resourcetype;

import static com.hmg.role.util.Constants.DELETED;
import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;
import static com.hmg.role.util.Constants.MAX_500_SIZE;

import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.interfaces.PolicyService;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import com.hmg.role.rbac.policy.policyitem.PolicyItemRepository;
import com.hmg.role.rbac.resourceaction.ResourceAction;
import com.hmg.role.rbac.resourceaction.ResourceActionMapper;
import com.hmg.role.rbac.resourceaction.ResourceActionRepository;
import com.hmg.role.rbac.resourcetag.ResourceTag;
import com.hmg.role.rbac.resourcetag.ResourceTagMapper;
import com.hmg.role.rbac.resourcetag.ResourceTagRepository;
import com.hmg.role.rbac.resourcetype.dto.CreateResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.DeleteBulkResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.ResourceTypeConflictDetailDto;
import com.hmg.role.rbac.resourcetype.dto.ResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.ResourceTypePageRequestDto;
import com.hmg.role.rbac.resourcetype.dto.UpdateBulkResourceTypeDto;
import com.hmg.role.rbac.resourcetype.dto.UpdateResourceTypeDto;
import com.hmg.role.rbac.resourcetype.enums.ResourceTypeSource;
import com.hmg.role.rbac.resourcetype.exceptions.ResourceTypeAlreadyExistException;
import com.hmg.role.rbac.resourcetype.exceptions.ResourceTypeBeingUsedException;
import com.hmg.role.rbac.resourcetype.exceptions.ResourceTypeDepthExceededException;
import com.hmg.role.rbac.resourcetype.exceptions.ResourceTypeNotFoundException;
import com.hmg.role.rbac.resourcetype.exceptions.TooManyResourceTypesException;
import com.hmg.role.rbac.resourcetype.interfaces.ResourceTypeService;
import com.hmg.role.rbac.resourcetype.projections.ResourceTypeParentProjection;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.exceptions.BadRequestException;
import com.hmg.role.util.exceptions.NotFoundException;
import com.hmg.role.util.exceptions.TypeNotFoundException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResourceTypeServiceImpl implements ResourceTypeService {

    private final ResourceTypeRepository resourceTypeRepository;
    private final ResourceActionRepository resourceActionRepository;
    private final PolicyItemRepository policyItemRepository;
    private final ResourceTagRepository resourceTagRepository;
    private final PolicyService policyService;

    private final AuditService auditService;

    private final ResourceTypeMapper resourceTypeMapper;

    private final ResourceActionMapper resourceActionMapper;
    private final ResourceTagMapper resourceTagMapper;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    private static final int MAX_RESOURCE_TYPE_INHERITANCE_DEPTH = 5;

    public ResourceTypeDto createResourceType(CreateResourceTypeDto createResourceTypeDto) {

        return createBulkResourceTypes(List.of(createResourceTypeDto)).results().getFirst();
    }

    public ListResponseDto<ResourceTypeDto> createBulkResourceTypes(
            List<CreateResourceTypeDto> createResourceTypeDtos) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        var resourceTypeKeys =
                createResourceTypeDtos.stream().map(CreateResourceTypeDto::key).toList();

        validateResourceTypeAlreadyExist(resourceTypeKeys, projectData);

        validateResourceTypeCountByProject(projectData, createResourceTypeDtos);

        // TODO: validate resource dataType depth
        var parentKeys =
                createResourceTypeDtos.stream()
                        .filter(rt -> StringUtils.isNotBlank(rt.parentKey()))
                        .map(CreateResourceTypeDto::parentKey)
                        .distinct()
                        .toList();

        if (!parentKeys.isEmpty()) {
            var parents =
                    resourceTypeRepository.findByKeyInAndProjectAndDeletedFalse(
                            parentKeys, projectData);

            validateParentResourceType(parentKeys, parents);

            parents.forEach(
                    parent -> {
                        var depth = countDepth(parent, 0);

                        if (depth >= MAX_RESOURCE_TYPE_INHERITANCE_DEPTH) {
                            throw new ResourceTypeDepthExceededException(
                                    depth + 1, MAX_RESOURCE_TYPE_INHERITANCE_DEPTH);
                        }
                    });
        }

        var responseResourceTypeDtoList =
                saveCreateBulkResourceType(createResourceTypeDtos, projectData);

        log.info(
                "Succesfully Created Resource Types: {} from Project: {}",
                resourceTypeKeys,
                projectData.getKey());

        return ListResponseDto.create(responseResourceTypeDtoList);
    }

    private int countDepth(ResourceType resourceType, int count) {
        if (resourceType == null) {
            return count;
        }

        return countDepth(resourceType.getParent(), count + 1);
    }

    private List<ResourceTypeDto> saveCreateBulkResourceType(
            List<CreateResourceTypeDto> dtoList, Project projectData) {

        // map create DTOs to ResourceType entities (w/o setting parent yet)
        Map<String, ResourceType> keyToResourceTypeMap = new HashMap<>();
        Set<String> externalParentKeys = new HashSet<>();
        // TODO: remove once AuditorAware is safely wired
        String authorKey = authorRequestScope.getMemberKey();

        for (CreateResourceTypeDto dto : dtoList) {
            var resourceType = resourceTypeMapper.toResourceType(dto, projectData);
            resourceType.setCreatedBy(authorKey);
            resourceType.setUpdatedBy(authorKey);
            keyToResourceTypeMap.put(dto.key(), resourceType);

            if (dto.parentKey() != null && !keyToResourceTypeMap.containsKey(dto.parentKey())) {
                externalParentKeys.add(dto.parentKey());
            }
        }

        // save all ResourceTypes first w/o parent (to generate ID)
        List<ResourceType> initialSaveList = new ArrayList<>(keyToResourceTypeMap.values());
        List<ResourceType> savedResourceTypeList = resourceTypeRepository.saveAll(initialSaveList);

        // fetch external parents from DB
        Map<String, ResourceType> externalParentMap =
                resourceTypeRepository
                        .findAllByKeyInAndProjectAndDeletedFalse(externalParentKeys, projectData)
                        .stream()
                        .collect(Collectors.toMap(ResourceType::getKey, Function.identity()));

        // Set parents
        Map<String, ResourceType> savedMap =
                savedResourceTypeList.stream()
                        .collect(Collectors.toMap(ResourceType::getKey, Function.identity()));

        for (CreateResourceTypeDto dto : dtoList) {
            var resourceType = savedMap.get(dto.key());
            String parentKey = dto.parentKey();

            if (parentKey != null) {
                ResourceType parent = savedMap.get(parentKey);

                if (parent == null) {
                    parent = externalParentMap.get(parentKey);
                } else {
                    parent = externalParentMap.get(parentKey);
                    if (parent == null) {
                        throw new NotFoundException("Parent ResourceType not found: " + parentKey);
                    }

                    if (!parent.getProject().equals(projectData)) {
                        throw new RuntimeException();
                    }
                }

                resourceType.setParent(parent);
            }
        }

        // Save again after setting parent references
        savedResourceTypeList = resourceTypeRepository.saveAll(savedMap.values());
        auditService.commitAsync(savedResourceTypeList);

        // Save Resource Actions
        List<ResourceAction> resourceActionsDataList = new ArrayList<>();
        Map<String, ResourceType> finalSavedMap =
                savedResourceTypeList.stream()
                        .collect(Collectors.toMap(ResourceType::getKey, Function.identity()));

        dtoList.forEach(
                dto -> {
                    var resourcetype = finalSavedMap.get(dto.key());
                    for (String action : dto.actions()) {
                        resourceActionsDataList.add(
                                resourceActionMapper.toResourceAction(resourcetype, action));
                    }
                });

        resourceActionRepository.saveAll(resourceActionsDataList);
        auditService.commitAsync(resourceActionsDataList);
        var resourceActionMap =
                resourceActionsDataList.stream()
                        .collect(Collectors.groupingBy(ResourceAction::getResourceType));

        // Save Resource Tags
        List<ResourceTag> resourceTagDataList = new ArrayList<>();
        dtoList.stream()
                .filter(dto -> !ObjectUtils.isEmpty(dto.tags()))
                .forEach(
                        dto -> {
                            var resourceType = finalSavedMap.get(dto.key());
                            for (String tag : dto.tags()) {
                                resourceTagDataList.add(
                                        resourceTagMapper.toResource(resourceType, tag));
                            }
                        });

        Map<ResourceType, List<ResourceTag>> resourceTagMap = new HashMap<>();
        if (!resourceTagDataList.isEmpty()) {
            var savedResourceTagList = resourceTagRepository.saveAll(resourceTagDataList);
            auditService.commitAsync(savedResourceTagList);
            resourceTagMap =
                    savedResourceTagList.stream()
                            .collect(Collectors.groupingBy(ResourceTag::getResourceType));
        }

        // Map to DTOs
        List<ResourceTypeDto> responseList = new ArrayList<>();
        for (ResourceType resourceType : savedResourceTypeList) {
            var actions = resourceActionMap.get(resourceType);
            var tags =
                    resourceTagMap.getOrDefault(resourceType, List.of()).stream()
                            .map(ResourceTag::getTag)
                            .toList();

            responseList.add(resourceTypeMapper.toResourceTypeDto(resourceType, actions, tags));
        }

        return responseList;
    }

    public ResourceTypeDto updateResourceType(
            UpdateResourceTypeDto updateResourceTypeDto, String resourceKey) {

        UpdateBulkResourceTypeDto updateBulkResourceTypeDto =
                UpdateBulkResourceTypeDto.builder()
                        .key(resourceKey)
                        .description(updateResourceTypeDto.description())
                        .actions(updateResourceTypeDto.actions())
                        .tags(updateResourceTypeDto.tags())
                        .parentKey(updateResourceTypeDto.parentKey())
                        .name(updateResourceTypeDto.name())
                        .build();

        return updateBulkResourceTypes(List.of(updateBulkResourceTypeDto)).results().getFirst();
    }

    public ListResponseDto<ResourceTypeDto> updateBulkResourceTypes(
            List<UpdateBulkResourceTypeDto> updateBulkResourceTypeDtos) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        var resourceTypeKeys =
                updateBulkResourceTypeDtos.stream().map(UpdateBulkResourceTypeDto::key).toList();

        var resourceTypeEntities =
                resourceTypeRepository.findByKeyInAndProjectAndDeletedFalse(
                        resourceTypeKeys, projectData);

        var parentKeys =
                updateBulkResourceTypeDtos.stream()
                        .map(UpdateBulkResourceTypeDto::parentKey)
                        .filter(Objects::nonNull)
                        .filter(p -> !p.isBlank())
                        .distinct()
                        .toList();

        var parentMap =
                resourceTypeRepository
                        .findByKeyInAndProjectAndDeletedFalse(
                                new ArrayList<>(parentKeys), projectData)
                        .stream()
                        .collect(Collectors.toMap(ResourceType::getKey, r -> r));

        validateResourceTypeNotFound(resourceTypeKeys, resourceTypeEntities);

        // TODO: validate resource dataType depth

        if (!parentKeys.isEmpty()) {
            var parents =
                    resourceTypeRepository.findByKeyInAndProjectAndDeletedFalse(
                            parentKeys, projectData);

            validateParentResourceType(parentKeys, parents);

            parents.forEach(
                    parent -> {
                        var depth = countDepth(parent, 0);

                        if (depth >= MAX_RESOURCE_TYPE_INHERITANCE_DEPTH) {
                            throw new ResourceTypeDepthExceededException(
                                    depth + 1, MAX_RESOURCE_TYPE_INHERITANCE_DEPTH);
                        }
                    });
        }

        var resourceTypeMap =
                resourceTypeEntities.stream()
                        .collect(
                                Collectors.toMap(
                                        ResourceType::getKey, resourceType -> resourceType));

        var resourceTypeDtoList =
                updateBulkResourceType(updateBulkResourceTypeDtos, resourceTypeMap, parentMap);

        log.info(
                "Successfully Updated Resource Types: {} from Project: {}",
                resourceTypeKeys,
                projectData.getKey());

        return ListResponseDto.create(resourceTypeDtoList);
    }

    private List<ResourceTypeDto> updateBulkResourceType(
            List<UpdateBulkResourceTypeDto> dtoList,
            Map<String, ResourceType> resourceTypeMap,
            Map<String, ResourceType> parentMap) {

        var resourceTypeList =
                dtoList.stream()
                        .map(
                                dto -> {
                                    var resourceType = resourceTypeMap.get(dto.key());
                                    resourceTypeMapper.toResourceType(resourceType, dto);
                                    resourceType.setUpdatedBy(authorRequestScope.getMemberKey());

                                    if (dto.parentKey() != null && !dto.parentKey().isBlank()) {
                                        if (!dto.parentKey().equals(resourceType.getKey())) {
                                            var newParent = parentMap.get(dto.parentKey());
                                            if (newParent == null) {
                                                throw new BadRequestException(
                                                        "Parent not found for key: "
                                                                + dto.parentKey());
                                            }

                                            if (newParent.getKey().equals(resourceType.getKey())) {
                                                throw new BadRequestException(
                                                        "Circular parent reference detected");
                                            }

                                            resourceType.setParent(newParent);
                                        } else {
                                            throw new BadRequestException(
                                                    "Resource dataType cannot be its own parent: "
                                                            + dto.key());
                                        }
                                    } else {
                                        resourceType.setParent(null); // explicit unparent
                                    }

                                    return resourceType;
                                })
                        .toList();

        var savedResourceTypeList = resourceTypeRepository.saveAll(resourceTypeList);
        auditService.commitAsync(savedResourceTypeList);

        var savedResourceTypeMap =
                resourceTypeList.stream()
                        .collect(
                                Collectors.toMap(
                                        ResourceType::getKey, resourceType -> resourceType));

        List<ResourceAction> savedResourceActionList = new ArrayList<>();

        dtoList.forEach(
                dto -> {
                    var resourceTypeData = savedResourceTypeMap.get(dto.key());
                    var resourceActionList = updateResourceActions(resourceTypeData, dto);

                    savedResourceActionList.addAll(resourceActionList);
                });

        var savedResourceActionMap =
                savedResourceActionList.stream()
                        .collect(Collectors.groupingBy(ResourceAction::getResourceType));

        // Checking Existing tag
        var tagCheck =
                resourceTagRepository.findAllByResourceTypeInAndDeletedFalse(resourceTypeList);

        if (!ObjectUtils.isEmpty(tagCheck)) {
            // Soft delete Existing tag
            String prefixDelete =
                    DELETED
                            + "-"
                            + ZonedDateTime.now()
                                    .format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT))
                            + "-";

            var softDeletedTagList =
                    tagCheck.stream()
                            .peek(tag -> tag.setTag(prefixDelete + tag.getTag()))
                            .peek(tag -> tag.setDeleted(true))
                            .toList();

            resourceTagRepository.saveAll(softDeletedTagList);

            auditService.commitAsync(softDeletedTagList);
        }

        List<ResourceTag> updatedResourceTagList =
                dtoList.stream()
                        .filter(dto -> !ObjectUtils.isEmpty(dto.tags()))
                        .flatMap(
                                dto -> {
                                    var resourceTypeData = savedResourceTypeMap.get(dto.key());

                                    return dto.tags().stream()
                                            .map(
                                                    tag ->
                                                            resourceTagMapper.toResource(
                                                                    resourceTypeData, tag));
                                })
                        .toList();

        var savedResourceTagList = resourceTagRepository.saveAll(updatedResourceTagList);

        auditService.commitAsync(savedResourceTagList);

        var savedResourceTagMap =
                savedResourceTagList.stream()
                        .collect(Collectors.groupingBy(ResourceTag::getResourceType));

        List<ResourceTypeDto> resourceTypeDtoList = new ArrayList<>();

        resourceTypeList.forEach(
                savedResourceType -> {
                    List<String> tagList = new ArrayList<>();

                    if (savedResourceTagMap.containsKey(savedResourceType)) {
                        var resourceTagData = savedResourceTagMap.get(savedResourceType);
                        tagList = resourceTagData.stream().map(ResourceTag::getTag).toList();
                    }
                    var resourceActionData = savedResourceActionMap.get(savedResourceType);

                    var resourceTypeDto =
                            resourceTypeMapper.toResourceTypeDto(
                                    savedResourceType, resourceActionData, tagList);

                    resourceTypeDtoList.add(resourceTypeDto);
                });

        return resourceTypeDtoList;
    }

    @Override
    public void deleteResourceType(String resourceKey) {
        deleteResourceTypesV2(List.of(resourceKey), false);
    }

    @Override
    public void deleteCascadeResourceType(String resourceKey) {
        deleteResourceTypesV2(List.of(resourceKey), true);
    }

    @Override
    public void deleteBulkResourceTypes(DeleteBulkResourceTypeDto deleteBulkResourceTypeDto) {
        deleteResourceTypesV2(deleteBulkResourceTypeDto.keys(), false);
    }

    @Override
    public void deleteCascadeBulkResourceTypes(
            DeleteBulkResourceTypeDto deleteBulkResourceTypeDto) {
        deleteResourceTypesV2(deleteBulkResourceTypeDto.keys(), true);
    }

    private void deleteResourceTypesV2(List<String> resourceTypeKeys, boolean cascade) {
        Project projectData = authorRequestScope.getProject();

        var initialResourceTypes =
                resourceTypeRepository.findByKeyInAndProjectAndDeletedFalse(
                        resourceTypeKeys, projectData);

        validateResourceTypeNotFound(resourceTypeKeys, initialResourceTypes);

        var policyItemList =
                policyItemRepository.findByResourceActionResourceTypeInAndPolicyDeletedIsFalse(
                        initialResourceTypes);

        if (!policyItemList.isEmpty()) {

            if (cascade) {
                log.debug("SoftDelete PolicyItem by resourceTypeKey: {}", resourceTypeKeys);
                policyService.deletePolicyItems(policyItemList);

            } else {

                List<String> existingPolicyKeys = new ArrayList<>();

                policyItemList.forEach(
                        policyItem -> {
                            existingPolicyKeys.add(policyItem.getPolicy().getKey());
                        });

                var projectKey = projectData.getKey();

                var conflictDto =
                        ResourceTypeConflictDetailDto.builder()
                                .policyKeys(existingPolicyKeys)
                                .resourceTypeKeys(resourceTypeKeys)
                                .projectKeys(List.of(projectKey))
                                .build();

                throw new ResourceTypeBeingUsedException(conflictDto);
            }
        }

        // Always check and reassign children before deleting the parent
        List<ResourceType> children =
                resourceTypeRepository.findByParentInAndProjectAndDeletedFalse(
                        initialResourceTypes, projectData);

        Set<String> allKeysToDelete = new HashSet<>(resourceTypeKeys);
        if (!children.isEmpty()) {
            List<ResourceType> updatedChildren = new ArrayList<>();

            children.forEach(
                    child -> {
                        var newChild = new ResourceType();
                        newChild.setId(child.getId());
                        newChild.setDescription(child.getDescription());
                        newChild.setKey(child.getKey());
                        newChild.setDeleted(child.isDeleted());
                        newChild.setProject(child.getProject());
                        newChild.setName(child.getName());
                        newChild.setParent(null);

                        updatedChildren.add(newChild);
                    });

            resourceTypeRepository.saveAll(updatedChildren);
        }

        var allResourceTypesToDelete =
                resourceTypeRepository.findByKeyInAndProjectAndDeletedFalse(
                        new ArrayList<>(allKeysToDelete), projectData);

        doDeleteResourceTypes(allResourceTypesToDelete, new ArrayList<>(allKeysToDelete));
    }

    private void doDeleteResourceTypes(
            List<ResourceType> resourceTypeEntities, List<String> resourceTypeKeys) {
        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        auditService.commitAsync(resourceTypeEntities);

        // Delete associated actions and tags
        softDeleteResourceActions(resourceTypeEntities, deletedDatetime);
        softDeleteResourceTags(resourceTypeEntities, resourceTypeKeys, deletedDatetime);
        softDeleteResourceTypes(resourceTypeEntities, resourceTypeKeys, deletedDatetime);

        String memberKey = authorRequestScope.getMemberKey();
        for (var resourceType : resourceTypeEntities) {
            resourceType.setUpdatedBy(memberKey);
        }

        resourceTypeRepository.saveAll(resourceTypeEntities);
    }

    private void softDeleteResourceTypes(
            List<ResourceType> resourceTypes,
            List<String> resourceTypeKeys,
            String deletedDatetime) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        softDeleteResourceActions(resourceTypes, deletedDatetime);

        // soft delete Resource tag if any
        softDeleteResourceTags(resourceTypes, resourceTypeKeys, deletedDatetime);

        for (var resourceType : resourceTypes) {
            resourceType.setKey(DELETED + "-" + deletedDatetime + "-" + resourceType.getKey());
            resourceType.setDeleted(true);
        }

        resourceTypeRepository.saveAll(resourceTypes);

        log.info(
                "Successfully Deleted Resource Types: {} from Project: {}",
                resourceTypeKeys,
                projectData.getKey());
    }

    private void softDeleteResourceTags(
            List<ResourceType> resourceTypes,
            List<String> resourceTypeKeys,
            String deletedDatetime) {

        String prefixDelete = DELETED + "-" + deletedDatetime + "-";

        var existingResourceTags =
                resourceTagRepository.findAllByResourceTypeInAndDeletedFalse(resourceTypes);

        if (ObjectUtils.isEmpty(existingResourceTags)) return;

        var tagList = existingResourceTags.stream().map(ResourceTag::getTag).toList();

        var updatedResourceTagList = new ArrayList<ResourceTag>();
        existingResourceTags.forEach(
                resourceTag -> {
                    resourceTag.setId(resourceTag.getId());
                    resourceTag.setResourceType(resourceTag.getResourceType());
                    resourceTag.setTag(prefixDelete + resourceTag.getTag());
                    resourceTag.setDeleted(true);

                    updatedResourceTagList.add(resourceTag);
                });

        resourceTagRepository.saveAll(updatedResourceTagList);
        auditService.commitAsync(updatedResourceTagList);

        log.info(
                "soft-deleted Resource Tag with given Resource Type Keys: {}, and Tags: {}",
                resourceTypeKeys,
                tagList);
    }

    private void softDeleteResourceActions(
            List<ResourceType> resourceTypes, String deletedDatetime) {

        String prefixDelete = DELETED + "-" + deletedDatetime + "-";

        var existingResourceActions =
                resourceActionRepository.findAllByResourceTypeInAndDeletedIsFalse(resourceTypes);

        var actionNames =
                existingResourceActions.stream().map(ResourceAction::getActionName).toList();
        var resourceTypeKeys =
                existingResourceActions.stream()
                        .map(ResourceAction::getResourceType)
                        .map(ResourceType::getKey)
                        .toList();

        for (var existingResourceAction : existingResourceActions) {
            existingResourceAction.setActionName(
                    prefixDelete + existingResourceAction.getActionName());
            existingResourceAction.setDeleted(true);
        }

        resourceActionRepository.saveAll(existingResourceActions);
        auditService.commitAsync(existingResourceActions);

        log.info(
                "soft-deleted Resource Action with given Resource Type Keys: {}, and Action Names: {}",
                resourceTypeKeys,
                actionNames);
    }

    @Transactional(readOnly = true)
    public ListResponseDto<ResourceTypeDto> listAllResourceTypes(ResourceTypePageRequestDto dto) {

        Project project =
                Optional.ofNullable(authorRequestScope.getProject())
                        .orElseGet(() -> authorRequestScope.getMember().getProject());

        String keyword = dto.getKeyword();
        String type = dto.getType();
        String source = dto.getSource();
        String andActionLike = dto.getAndActionLike();
        String andActionLikeEscaped = escapeLike(andActionLike);

        Specification<ResourceType> spec =
                Specification.where(ResourceTypeSpecification.projectIs(project))
                        .and(ResourceTypeSpecification.notDeleted());

        source = source == null ? "resource_type" : source;
        ResourceTypeSource tabSource = ResourceTypeSource.valueOf(source.toUpperCase());

        if (StringUtils.isAnyBlank(keyword, type)) {
            spec.and(ResourceTypeSpecification.isRoot());

            if (!StringUtils.isBlank(andActionLike)) {
                spec = spec.and(ResourceTypeSpecification.actionNameLike(andActionLike));
            }

            Specification<ResourceType> orderedSpec =
                    spec.and(ResourceTypeOrderingSpec.withBucketedOrder());
            var pageRequest = PageRequest.of(dto.getPage(), dto.getSize());
            Page<ResourceType> page = resourceTypeRepository.findAll(orderedSpec, pageRequest);

            int numberOfResourceType = 0;
            if (!StringUtils.isBlank(andActionLike)) {
                numberOfResourceType =
                        resourceTypeRepository
                                .countResourceTypeByProjectAndActionLikeAndDeletedFalse(
                                        project, andActionLikeEscaped);
            } else {
                numberOfResourceType =
                        resourceTypeRepository.countResourceTypeByProjectAndDeletedFalse(project);
            }

            // remove the duplication
            List<ResourceTypeDto> result =
                    page.stream()
                            .filter(Objects::nonNull)
                            .map(this::getParent)
                            .map(
                                    k ->
                                            tabSource == ResourceTypeSource.POLICY
                                                    ? removeFilteredActionItems(k, andActionLike)
                                                    : k)
                            .map(
                                    k ->
                                            tabSource == ResourceTypeSource.POLICY
                                                    ? removeFilteredActionItemsRecursively(
                                                            k, andActionLike)
                                                    : k)
                            .map(resourceTypeMapper::toResourceTypeDtoTree)
                            .filter(dtoData -> dtoData.parentKey() == null)
                            .filter(distinctByKey(ResourceTypeDto::key))
                            .collect(Collectors.toList());

            Page<ResourceTypeDto> dtoPage =
                    new PageImpl<>(result, pageRequest, numberOfResourceType);

            return ListResponseDto.create(dtoPage, numberOfResourceType);
        }

        // Base spec
        String kwRaw = keyword.toLowerCase();
        String kw = escapeLike(kwRaw);
        ResourceTypeFieldSearch searchType = ResourceTypeFieldSearch.valueOf(type.toUpperCase());

        int numberOfResourceType = 0;

        // add search-specific filters
        switch (searchType) {
            // TODO explode this into individual parameters
            // and replace the queries with ":x IS NULL or x = :x
            case NAME:
                spec =
                        spec.and(
                                ResourceTypeSpecification.nameLike(kw)
                                        .or(ResourceTypeSpecification.childNameLike(kw)));

                numberOfResourceType =
                        resourceTypeRepository.countResourceTypeByProjectAndNameAndDeletedFalse(
                                project, kw);
                break;
            case KEY:
                spec =
                        spec.and(
                                ResourceTypeSpecification.keyLike(kw)
                                        .or(ResourceTypeSpecification.childKeyLike(kw)));

                numberOfResourceType =
                        resourceTypeRepository.countResourceTypeByProjectAndKeyAndDeletedFalse(
                                project, kw);
                break;
            case TAG:
                spec = spec.and(ResourceTypeSpecification.hasTagLike(kw));

                numberOfResourceType =
                        resourceTypeRepository.countResourceTypeByProjectAndTagAndDeletedFalse(
                                project, kw);
                break;
            case NAME_TAG:
                spec =
                        spec.and(
                                ResourceTypeSpecification.nameLike(kw)
                                        .or(ResourceTypeSpecification.hasTagLike(kw)));

                numberOfResourceType =
                        resourceTypeRepository
                                .countResourceTypeByProjectAndNameOrTagAndDeletedFalse(project, kw);
                break;
            case ACTION:
                spec = spec.and(ResourceTypeSpecification.actionNameLike(kwRaw));

                numberOfResourceType =
                        resourceTypeRepository.countResourceTypeByProjectAndActionAndDeletedFalse(
                                project, kw);
                break;
            default:
                throw new TypeNotFoundException();
        }

        if (!StringUtils.isBlank(andActionLike)) {
            spec = spec.and(ResourceTypeSpecification.actionNameLike(andActionLike));
            numberOfResourceType =
                    switch (searchType) { // TODO remove this counting
                        case NAME ->
                                resourceTypeRepository
                                        .countResourceTypeByProjectAndNameAndActionLikeAndDeletedFalse(
                                                project, kw, andActionLikeEscaped);
                        case KEY ->
                                resourceTypeRepository
                                        .countResourceTypeByProjectAndKeyAndActionLikeAndDeletedFalse(
                                                project, kw, andActionLikeEscaped);
                        case TAG ->
                                resourceTypeRepository
                                        .countResourceTypeByProjectAndTagAndActionLikeAndDeletedFalse(
                                                project, kw, andActionLikeEscaped);
                        case NAME_TAG ->
                                resourceTypeRepository
                                        .countResourceTypeByProjectAndNameOrTagAndActionLikeAndDeletedFalse(
                                                project, kw, andActionLikeEscaped);
                        default -> throw new TypeNotFoundException();
                    };
        }

        // execute
        Specification<ResourceType> orderedSpec =
                spec.and(ResourceTypeOrderingSpec.withBucketedOrder());
        var pageRequest = PageRequest.of(dto.getPage(), dto.getSize());
        Page<ResourceType> page = resourceTypeRepository.findAll(orderedSpec, pageRequest);

        var resultDtoStream = page.stream();

        List<ResourceTypeDto> resultDtos =
                resultDtoStream
                        .map(this::getParent)
                        .map(
                                k ->
                                        tabSource == ResourceTypeSource.POLICY
                                                ? removeFilteredActionItems(k, andActionLike)
                                                : k)
                        .map(rt -> prune(rt, searchType, tabSource, kwRaw, andActionLike))
                        .map(resourceTypeMapper::toResourceTypeDtoTree)
                        .filter(dtoData -> dtoData.parentKey() == null)
                        .filter(distinctByKey(ResourceTypeDto::key))
                        .toList();

        Page<ResourceTypeDto> dtoPage =
                new PageImpl<>(resultDtos, pageRequest, numberOfResourceType);

        return ListResponseDto.create(dtoPage, numberOfResourceType);
    }

    private static String escapeLike(String input) {
        if (input == null) return null;
        return input.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Recursively prunes a ResourceType tree so that only nodes matching the specified search
     * criteria (or having matching descendants) remain.
     *
     * <p>Algorithm: 1. Recursively process and prune all children. 2. Evaluate whether this node
     * matches based on the search dataType: - ACTION: filters its actions list. - TAG: filters its
     * tags list. - NAME/KEY/NAME_TAG: checks field containment. 3. If neither the node nor any of
     * its pruned children match, return null. 4. Otherwise, attach the pruned children to this node
     * and return it.
     *
     * @param rt
     * @param search
     * @param tabScreen
     * @param kw
     * @return
     */
    private ResourceType prune(
            ResourceType rt,
            ResourceTypeFieldSearch search,
            ResourceTypeSource tabScreen,
            String kw,
            String andActionLike) {
        // 1) Prune children recursively
        List<ResourceType> prunedChildren =
                rt.getChildren().stream()
                        .map(k -> removeFilteredActionItems(k, andActionLike))
                        .map(child -> prune(child, search, tabScreen, kw, andActionLike))
                        .toList();

        boolean parentMatches;
        switch (search) {
            case ACTION -> {
                List<ResourceAction> original = new ArrayList<>(rt.getResourceActions());
                // only those matching keyword
                List<ResourceAction> matched = new ArrayList<>();

                if (tabScreen == ResourceTypeSource.POLICY) {
                    matched =
                            original.stream()
                                    .filter(
                                            a ->
                                                    !a.isDeleted()
                                                            && a.getActionName()
                                                                    .toLowerCase()
                                                                    .contains(kw))
                                    .toList();

                    parentMatches = !matched.isEmpty();

                    rt.setResourceActions(parentMatches ? matched : List.of());
                } else {

                    parentMatches =
                            original.stream()
                                    .anyMatch(
                                            ra ->
                                                    !ra.isDeleted()
                                                            && ra.getActionName()
                                                                    .toLowerCase()
                                                                    .contains(kw));

                    var filteredOriginal = original.stream().filter(ra -> !ra.isDeleted()).toList();

                    rt.setResourceActions(parentMatches ? filteredOriginal : List.of());
                }
            }
            case TAG -> {
                // original list
                List<ResourceTag> original = new ArrayList<>(rt.getResourceTags());
                // only those matching keyword
                List<ResourceTag> matched =
                        original.stream()
                                .filter(
                                        t ->
                                                !t.isDeleted()
                                                        && t.getTag().toLowerCase().contains(kw))
                                .toList();
                parentMatches = !matched.isEmpty();
                // if parent matched, show only matched; otherwise leave full list
                rt.setResourceTags(parentMatches ? matched : List.of());
            }
            case NAME_TAG -> {
                List<ResourceTag> original = new ArrayList<>(rt.getResourceTags());
                // only those matching keyword
                List<ResourceTag> matched =
                        original.stream()
                                .filter(
                                        t ->
                                                !t.isDeleted()
                                                        && t.getTag().toLowerCase().contains(kw))
                                .toList();

                rt.setResourceTags(matched);
            }
        }

        // 2) Otherwise wire in the pruned children and return
        rt.setChildren(prunedChildren);
        return rt;
    }

    private ResourceType removeFilteredActionItemsRecursively(
            ResourceType r, String andActionLike) {
        // TODO refactor this
        if (!StringUtils.isBlank(andActionLike)) {
            List<ResourceType> appliedChildren =
                    r.getChildren().stream()
                            .map(kc -> removeFilteredActionItemsRecursively(kc, andActionLike))
                            .toList();

            r.setResourceActions(
                    r.getResourceActions().stream()
                            .filter(
                                    e ->
                                            e.getActionName()
                                                    .toLowerCase()
                                                    .contains(andActionLike.toLowerCase()))
                            .toList());

            r.setChildren(appliedChildren);
        }
        return r;
    }

    private static ResourceType removeFilteredActionItems(ResourceType r, String s) {
        // TODO refactor this
        if (!StringUtils.isBlank(s)) {
            r.setResourceActions(
                    r.getResourceActions().stream()
                            .filter(k -> k.getActionName().toLowerCase().contains(s.toLowerCase()))
                            .toList());
        }
        return r;
    }

    private ResourceType getParent(ResourceType resourceType) {
        if (resourceType.getParent() == null) {
            return resourceType;
        }

        return getParent(resourceType.getParent());
    }

    private ResourceType getByKey(String resourceTypeKey) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        return resourceTypeRepository
                .findByKeyAndProjectAndDeletedFalse(resourceTypeKey, projectData)
                .orElseThrow(() -> new ResourceTypeNotFoundException(resourceTypeKey));
    }

    public ResourceTypeDto getResourceTypeByKey(String resourceTypeKey) {

        var resourceTypeEntity = getByKey(resourceTypeKey);

        var resourceActionEntities =
                resourceActionRepository.findAllByResourceTypeAndDeletedIsFalse(resourceTypeEntity);

        var resourceTagEntities =
                resourceTagRepository.findByResourceTypeAndDeletedFalse(resourceTypeEntity);

        List<String> tagList = null;
        if (!ObjectUtils.isEmpty(resourceTagEntities)) {
            tagList = resourceTagEntities.stream().map(ResourceTag::getTag).toList();
        }

        return resourceTypeMapper.toResourceTypeDto(
                resourceTypeEntity, resourceActionEntities, tagList);
    }

    private List<ResourceAction> updateResourceActions(
            ResourceType resourceTypeData, UpdateBulkResourceTypeDto updateResourceTypeDto) {

        return new ResourceActionsUpdateHandler(resourceTypeData, updateResourceTypeDto)
                .updateResourceActions();
    }

    private void validateResourceTypeAlreadyExist(
            List<String> newResourceTypeKeys, Project projectData) {

        var existingResourceTypes =
                resourceTypeRepository.findByKeyInAndProjectAndDeletedFalse(
                        newResourceTypeKeys, projectData);

        if (!existingResourceTypes.isEmpty()) {
            var existingKeys =
                    existingResourceTypes.stream()
                            .map(ResourceType::getKey)
                            .collect(Collectors.toSet());

            throw new ResourceTypeAlreadyExistException(String.valueOf(existingKeys));
        }

        Map<String, Long> frequencies =
                newResourceTypeKeys.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        frequencies.forEach(
                (key, count) -> {
                    if (count > 1) {
                        throw new ResourceTypeAlreadyExistException(key);
                    }
                });
    }

    private void validateResourceTypeNotFound(
            List<String> resourceTypeKeys, List<ResourceType> resourceTypes) {

        var existingResourceTypeKeys = resourceTypes.stream().map(ResourceType::getKey).toList();

        var nonExistentResourceTypeKeys =
                resourceTypeKeys.stream()
                        .filter(
                                resourceTypeKey ->
                                        !existingResourceTypeKeys.contains(resourceTypeKey))
                        .toList();

        if (!nonExistentResourceTypeKeys.isEmpty()) {
            log.error("Resource Type {} Is Not Found", nonExistentResourceTypeKeys);
            throw new ResourceTypeNotFoundException(nonExistentResourceTypeKeys);
        }
    }

    private void validateResourceTypeCountByProject(
            Project project, List<CreateResourceTypeDto> resourceTypeDtos) {
        // make sure that resource dataType count of the project doesn't exceed MAX_500_SIZE after
        // insertion

        int countExistingProjectResourceType =
                resourceTypeRepository.countResourceTypeByProjectAndDeletedFalse(project);
        int countTotalProjectResourceType =
                countExistingProjectResourceType + resourceTypeDtos.size();

        if (countTotalProjectResourceType > MAX_500_SIZE) {
            log.error(
                    "Too many resource types for the project. Total count: {}, count limit: {}",
                    countTotalProjectResourceType,
                    MAX_500_SIZE);
            throw new TooManyResourceTypesException();
        }
    }

    private void validateParentResourceType(List<String> keys, List<ResourceType> resourceTypes) {

        var existingKeys = resourceTypes.stream().map(ResourceType::getKey).toList();

        var nonExistentKeys = keys.stream().filter(key -> !existingKeys.contains(key)).toList();

        if (!nonExistentKeys.isEmpty()) {
            log.error("Parent of Resource Type {} Is Not Found", nonExistentKeys);
            throw new ResourceTypeNotFoundException(nonExistentKeys);
        }
    }

    private class ResourceActionsUpdateHandler {

        ResourceType resourceType;
        List<String> updateActionsNames;
        Map<String, ResourceAction> existingResourceActionMap;

        public ResourceActionsUpdateHandler(
                ResourceType resourceType, UpdateResourceTypeDto updateResourceTypeDto) {

            this.resourceType = resourceType;
            updateActionsNames = updateResourceTypeDto.actions();

            existingResourceActionMap =
                    resourceActionRepository
                            .findAllByResourceTypeAndDeletedIsFalse(resourceType)
                            .stream()
                            .collect(
                                    Collectors.toMap(
                                            ResourceAction::getActionName, Function.identity()));
        }

        public ResourceActionsUpdateHandler(
                ResourceType resourceType, UpdateBulkResourceTypeDto updateBulkResourceTypeDto) {

            this.resourceType = resourceType;
            updateActionsNames = updateBulkResourceTypeDto.actions();

            existingResourceActionMap =
                    resourceActionRepository
                            .findAllByResourceTypeAndDeletedIsFalse(resourceType)
                            .stream()
                            .collect(
                                    Collectors.toMap(
                                            ResourceAction::getActionName, Function.identity()));
        }

        public List<ResourceAction> updateResourceActions() {

            // soft-delete all old resource-actions that are not in request dto
            softDeleteResourceActionsIfNeeded();

            // insert all new resource-actions
            var allNew = saveNewResourceActionsIfNeeded();

            // existing resource-actions to be kept, no-operation
            var allKeptNoOps = getResourceActionsForNoOps();

            // return the final resource-actions, all new + all kept
            var finalResourceActions = new ArrayList<>(allKeptNoOps);
            finalResourceActions.addAll(allNew);
            return finalResourceActions;
        }

        private List<ResourceAction> getResourceActionsForNoOps() {
            return existingResourceActionMap.values().stream()
                    .filter(ra -> updateActionsNames.contains(ra.getActionName()))
                    .toList();
        }

        private List<ResourceAction> saveNewResourceActionsIfNeeded() {
            return streamOfActionsForInsert()
                    .map(action -> resourceActionMapper.toResourceAction(resourceType, action))
                    .map(resourceActionRepository::save)
                    .peek(auditService::commitAsync)
                    .toList();
        }

        private Stream<String> streamOfActionsForInsert() {
            return updateActionsNames.stream()
                    .filter(action -> !existingResourceActionMap.containsKey(action));
        }

        private List<ResourceAction> softDeleteResourceActionsIfNeeded() {

            // collect all existing action names that are going to be deleted
            // delete -> action that exists in db but not in the request update payload
            Project projectData = authorRequestScope.getProject();
            if (projectData == null) {
                projectData = authorRequestScope.getMember().getProject();
            }

            var actionsForDelete =
                    existingResourceActionMap.keySet().stream()
                            .filter(action -> !updateActionsNames.contains(action))
                            .toList();

            if (actionsForDelete.isEmpty()) {
                // means, nothing to delete
                return List.of();
            }

            // throw an exception if any policy item relies on it
            var relatedPolicyItems = existsActivePolicyForResourceTypeAndAction(actionsForDelete);
            if (!relatedPolicyItems.isEmpty()) {
                var projectKey = projectData.getKey();

                Set<String> resourceTypeKeys = new HashSet<>();
                Set<String> policyKeys = new HashSet<>();

                relatedPolicyItems.forEach(
                        policyItem -> {
                            var resourceTypeKey =
                                    policyItem.getResourceAction().getResourceType().getKey();
                            var policyKey = policyItem.getPolicy().getKey();

                            resourceTypeKeys.add(resourceTypeKey);
                            policyKeys.add(policyKey);
                        });

                var conflictDetail =
                        ResourceTypeConflictDetailDto.builder()
                                .policyKeys(policyKeys)
                                .resourceTypeKeys(resourceTypeKeys)
                                .projectKeys(List.of(projectKey))
                                .build();

                throw new ResourceTypeBeingUsedException(conflictDetail);
            }

            // apply soft delete to DB
            return softDeleteResourceActions(actionsForDelete);
        }

        private List<PolicyItem> existsActivePolicyForResourceTypeAndAction(
                List<String> actionsForDelete) {
            return policyItemRepository
                    .findByPolicyDeletedIsFalseAndResourceActionResourceTypeAndResourceActionActionNameIn(
                            resourceType, actionsForDelete);
        }

        private List<ResourceAction> softDeleteResourceActions(List<String> actionsForDelete) {

            String prefixDelete =
                    DELETED
                            + "-"
                            + ZonedDateTime.now()
                                    .format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT))
                            + "-";

            return existingResourceActionMap.values().stream()
                    .filter(
                            resourceAction ->
                                    actionsForDelete.contains(resourceAction.getActionName()))
                    .peek(ra -> ra.setActionName(prefixDelete + ra.getActionName()))
                    .peek(ra -> ra.setDeleted(true))
                    .map(resourceActionRepository::save)
                    .peek(
                            ra -> {
                                auditService.commitAsync(ra);

                                log.info(
                                        "soft-deleted ResourceAction, resourceTypeKey: {}, id: {}, name: {}, ",
                                        resourceType.getKey(),
                                        ra.getId(),
                                        ra.getActionName());
                            })
                    .toList();
        }
    }

    public ListResponseDto<ResourceTypeParentProjection> getAvailableParentKeys(
            String currentResourceTypeKey) {
        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        List<ResourceTypeParentProjection> allParentKeys =
                resourceTypeRepository.findOtherThan(projectData, currentResourceTypeKey);

        // Only expose resource types at depth < MAX_RESOURCE_TYPE_INHERITANCE_DEPTH as valid
        // parents,
        // so the new child stays within the max inheritance depth
        List<String> candidateKeys =
                allParentKeys.stream().map(ResourceTypeParentProjection::getKey).toList();

        Set<String> allowedParentKeys =
                resourceTypeRepository
                        .findByKeyInAndProjectAndDeletedFalse(candidateKeys, projectData)
                        .stream()
                        .filter(rt -> countDepth(rt, 0) < MAX_RESOURCE_TYPE_INHERITANCE_DEPTH)
                        .map(ResourceType::getKey)
                        .collect(java.util.stream.Collectors.toSet());

        List<ResourceTypeParentProjection> filtered =
                allParentKeys.stream().filter(p -> allowedParentKeys.contains(p.getKey())).toList();

        return ListResponseDto.create(filtered);
    }
}
