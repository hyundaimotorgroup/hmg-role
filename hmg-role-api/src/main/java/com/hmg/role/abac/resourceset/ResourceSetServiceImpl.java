package com.hmg.role.abac.resourceset;

import static com.hmg.role.util.Constants.CASCADE_DISABLED;
import static com.hmg.role.util.Constants.CASCADE_ENABLED;
import static com.hmg.role.util.Constants.DELETED;
import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;
import static com.hmg.role.util.Constants.MAX_500_SIZE;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.abac.logicalexpression.dto.OperandDto;
import com.hmg.role.abac.policy.AbacPolicy;
import com.hmg.role.abac.policy.AbacPolicyItem;
import com.hmg.role.abac.policy.AbacPolicyRepository;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemRepository;
import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.resourceset.action.ResourceSetActionMapper;
import com.hmg.role.abac.resourceset.action.ResourceSetActionRepository;
import com.hmg.role.abac.resourceset.condition.ResourceSetCondition;
import com.hmg.role.abac.resourceset.condition.ResourceSetConditionRepository;
import com.hmg.role.abac.resourceset.dto.DeleteBulkResourceSetDto;
import com.hmg.role.abac.resourceset.dto.ResourceSetDto;
import com.hmg.role.abac.resourceset.dto.SearchResourceSetDto;
import com.hmg.role.abac.resourceset.dto.UpdateBulkResourceSetDto;
import com.hmg.role.abac.resourceset.dto.UpdateResourceSetDto;
import com.hmg.role.abac.resourceset.exceptions.ParentResourceSetNotFoundException;
import com.hmg.role.abac.resourceset.exceptions.ResourceSetActionIsBeingUsedException;
import com.hmg.role.abac.resourceset.exceptions.ResourceSetAlreadyExist;
import com.hmg.role.abac.resourceset.exceptions.ResourceSetIsBeingUsedException;
import com.hmg.role.abac.resourceset.exceptions.ResourceSetNotFoundException;
import com.hmg.role.abac.resourceset.exceptions.ResourceSetTooManyException;
import com.hmg.role.abac.resourceset.interfaces.ResourceSetService;
import com.hmg.role.abac.resourceset.operand.ResourceSetOperand;
import com.hmg.role.abac.resourceset.operand.ResourceSetOperandMapper;
import com.hmg.role.abac.resourceset.operand.ResourceSetOperandRepository;
import com.hmg.role.abac.userset.attributes.AbacAttributeService;
import com.hmg.role.abac.userset.attributes.ConditionOperand;
import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.ProjectMapper;
import com.hmg.role.admin.project.dto.ProjectDto;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.enums.OperandPosition;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class ResourceSetServiceImpl implements ResourceSetService {

    private final ResourceSetRepository resourceSetRepository;
    private final ResourceSetConditionRepository resourceSetConditionRepository;
    private final ResourceSetOperandRepository resourceSetOperandRepository;
    private final ResourceSetActionRepository resourceSetActionRepository;
    private final AbacPolicyItemRepository abacPolicyItemRepository;
    private final AbacPolicyRepository abacPolicyRepository;

    private final AbacAttributeService attributeService;

    private final ResourceSetMapper resourceSetMapper;
    private final ResourceSetOperandMapper resourceSetOperandMapper;
    private final ResourceSetActionMapper resourceSetActionMapper;
    private final ProjectMapper projectMapper;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    public ResourceSetDto createResourceSet(ResourceSetDto resourceSetDto) {

        var resourceSetDtoList = List.of(resourceSetDto);
        return createResourceSetProcess(resourceSetDtoList).getFirst();
    }

    public ListResponseDto<ResourceSetDto> createBulkResourceSets(
            List<ResourceSetDto> resourceSetDtoList) {

        var createResourceSetResult = createResourceSetProcess(resourceSetDtoList);

        return ListResponseDto.create(createResourceSetResult);
    }

    // TODO: Need to fix bug. replaced instead create new resource set
    private List<ResourceSetDto> createResourceSetProcess(List<ResourceSetDto> resourceSetDtoList) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        validateExistingCreateBulkResourceSet(resourceSetDtoList, projectData);
        validateCreateBulkResourceSet(resourceSetDtoList, projectData);

        final Project projectDataFinal = projectData;
        var savedResourceSets =
                resourceSetDtoList.stream()
                        .map(dto -> mappingAndSaveNewResourceSet(dto, projectDataFinal))
                        .toList();

        var requestedResourceSetKeyList =
                resourceSetDtoList.stream().map(ResourceSetDto::key).toList();

        log.info(
                "Successfully Created Resource Sets: {} from Project: {}",
                requestedResourceSetKeyList,
                projectData.getKey());

        return savedResourceSets.stream().map(this::resourceSetMappingProcess).toList();
    }

    public ListResponseDto<ResourceSetDto> getResourceSets(SearchResourceSetDto searchDto) {

        Project projectData = getProject();

        String keyLike = escapeLike(searchDto.getKeyLike());
        String nameLike = escapeLike(searchDto.getNameLike());
        String actionLike = escapeLike(searchDto.getActionLike());

        var resourceSetEntities =
                resourceSetRepository.findBySearchParametersAndDeletedFalse(
                        keyLike, nameLike, actionLike, projectData, searchDto.pageRequest());

        List<ResourceSetDto> resourceSetDtoList =
                resourceSetEntities.stream().map(this::resourceSetMappingProcess).toList();

        var resourceSetDtoPage =
                new PageImpl<>(
                        resourceSetDtoList,
                        searchDto.pageRequest(),
                        resourceSetEntities.getTotalElements());

        var projectDto = getProjectDto();
        return ListResponseDto.create(resourceSetDtoPage, projectDto);
    }

    private ResourceSetDto resourceSetMappingProcess(ResourceSet resourceSetEntity) {

        var conditionSetDtos =
                resourceSetOperandMapper.toResourceConditionList(
                        resourceSetEntity.getConditionGroup());

        resourceSetEntity.setActions(
                resourceSetEntity.getActions().stream().filter(a -> !a.isDeleted()).toList());

        return resourceSetMapper.toResourceSetDto(resourceSetEntity, conditionSetDtos);
    }

    public ResourceSetDto getResourceSetByKey(String resourceSetKey) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        var resourceSetEntity =
                resourceSetRepository
                        .findByKeyAndProjectAndDeletedFalse(resourceSetKey, projectData)
                        .orElseThrow(ResourceSetNotFoundException::new);

        // Getting condition set, condition key, and actions
        var conditionSetDtoList =
                resourceSetOperandMapper.toResourceConditionList(
                        resourceSetEntity.getConditionGroup());

        resourceSetEntity.setActions(
                resourceSetEntity.getActions().stream().filter(a -> !a.isDeleted()).toList());

        return resourceSetMapper.toResourceSetDto(resourceSetEntity, conditionSetDtoList);
    }

    @Override
    public ResourceSetDto updateResourceSet(String resourceSetKey, UpdateResourceSetDto dto) {

        Project projectData = getProject();

        ResourceSet resourceSetEntity =
                resourceSetRepository
                        .findByKeyAndProjectAndDeletedFalse(resourceSetKey, projectData)
                        .orElseThrow(ResourceSetNotFoundException::new);

        // Delete existing conditions and operands; actions are merged to preserve IDs
        deleteResourceSetConditions(resourceSetEntity);

        var requestedConditionGroupList = dto.conditionGroup();

        resourceSetMapper.toResourceSet(resourceSetEntity, dto);
        resourceSetEntity.setUpdatedBy(authorRequestScope.getMemberKey());

        List<ResourceSetCondition> savedConditions = new ArrayList<>();
        for (var requestedConditionGroup : requestedConditionGroupList) {
            var savedCondition =
                    saveUpdatedResourceSetCondition(
                            requestedConditionGroup, resourceSetEntity, projectData);
            savedConditions.add(savedCondition);
        }
        resourceSetEntity.setConditionGroup(savedConditions);

        mergeResourceSetActions(dto.actions(), resourceSetEntity, projectData);

        ResourceSetDto resourceSetDto =
                resourceSetMapper.toResourceSetDto(resourceSetKey, dto, null);
        if (resourceSetEntity.getParent() != null) {
            resourceSetDto =
                    resourceSetMapper.toResourceSetDto(
                            resourceSetKey, dto, resourceSetEntity.getParent().getKey());
        }

        log.info(
                "Successfully Updated Resource Set: {} from Project: {}",
                resourceSetKey,
                projectData.getKey());

        return resourceSetDto;
    }

    @Override
    public ListResponseDto<ResourceSetDto> updateBulkResourceSets(
            List<UpdateBulkResourceSetDto> updateBulkResourceSetDtoList) {

        Project projectData = getProject();

        var requestedResourceSetKeyList =
                updateBulkResourceSetDtoList.stream().map(UpdateBulkResourceSetDto::key).toList();

        var resourceSetEntities =
                resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(
                        requestedResourceSetKeyList, projectData);

        var existingResourceSetKeyList =
                resourceSetEntities.stream().map(ResourceSet::getKey).toList();

        validateResourceSetNotFound(requestedResourceSetKeyList, existingResourceSetKeyList);

        var resourceSetEntityMap =
                resourceSetEntities.stream()
                        .collect(
                                Collectors.toMap(
                                        ResourceSet::getKey,
                                        resourceSetEntity -> resourceSetEntity));

        List<ResourceSetDto> resourceSetDtoList = new ArrayList<>();

        for (var updateBulkResourceSetDto : updateBulkResourceSetDtoList) {

            var resourceSetEntity = resourceSetEntityMap.get(updateBulkResourceSetDto.key());

            // Delete existing conditions and operands; actions are merged to preserve IDs
            deleteResourceSetConditions(resourceSetEntity);

            var requestedConditionGroupList = updateBulkResourceSetDto.conditionGroup();

            resourceSetMapper.toResourceSet(resourceSetEntity, updateBulkResourceSetDto);
            resourceSetEntity.setUpdatedBy(authorRequestScope.getMemberKey());

            List<ResourceSetCondition> savedConditions = new ArrayList<>();
            for (var requestedConditionGroup : requestedConditionGroupList) {
                var savedCondition =
                        saveUpdatedResourceSetCondition(
                                requestedConditionGroup, resourceSetEntity, projectData);
                savedConditions.add(savedCondition);
            }
            resourceSetEntity.setConditionGroup(savedConditions);

            var resourceSetDto =
                    saveUpdatedResourceSet(
                            updateBulkResourceSetDto, resourceSetEntity, projectData);

            resourceSetDtoList.add(resourceSetDto);
        }

        log.info(
                "Successfully Updated Resource Sets: {} from Project: {}",
                existingResourceSetKeyList,
                projectData.getKey());

        return ListResponseDto.create(resourceSetDtoList);
    }

    @Override
    public void deleteResourceSet(String resourceSetKey) {
        deleteBulkResourceSets(List.of(resourceSetKey), CASCADE_DISABLED);
    }

    @Override
    public void deleteResourceSetCascade(String resourceSetKey) {
        deleteBulkResourceSets(List.of(resourceSetKey), CASCADE_ENABLED);
    }

    @Override
    public void deleteBulkResourceSets(DeleteBulkResourceSetDto deleteBulkResourceSetDto) {
        var requestedResourceSetKeys = deleteBulkResourceSetDto.keys();

        deleteBulkResourceSets(requestedResourceSetKeys, CASCADE_DISABLED);
    }

    @Override
    public void deleteBulkResourceSetsCascade(DeleteBulkResourceSetDto deleteBulkResourceSetDto) {
        var requestedResourceSetKeys = deleteBulkResourceSetDto.keys();

        deleteBulkResourceSets(requestedResourceSetKeys, CASCADE_ENABLED);
    }

    private void deleteBulkResourceSets(List<String> resourceSetKeyList, boolean cascade) {

        Project projectData = getProject();

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        var existingResourceSetList =
                resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(
                        resourceSetKeyList, projectData);

        var existingResourceSetKeyList =
                existingResourceSetList.stream().map(ResourceSet::getKey).toList();

        // validations and delete process

        validateResourceSetNotFound(resourceSetKeyList, existingResourceSetKeyList);
        validateAndUpdatePolicyItems(existingResourceSetList, deletedDatetime, cascade);
        validateAndUpdateResourceSetHasChildren(existingResourceSetList, cascade);
        softDeleteResourceSets(
                existingResourceSetKeyList, existingResourceSetList, deletedDatetime);
    }

    private void validateAndUpdatePolicyItems(
            List<ResourceSet> existingResourceSets, String deletedDatetime, boolean cascade) {

        var policyItems =
                abacPolicyItemRepository
                        .findByResourceSetAction_ResourceSetInAndPolicy_DeletedFalse(
                                existingResourceSets);

        var isCascadeAndPolicyItemsIsNotEmpty = cascade && !policyItems.isEmpty();

        if (isCascadeAndPolicyItemsIsNotEmpty) {
            var existingResourceSetKeys =
                    existingResourceSets.stream().map(ResourceSet::getKey).toList();
            softDeletePolicy(policyItems, existingResourceSetKeys, deletedDatetime);
        } else if (!policyItems.isEmpty()) {
            throw new ResourceSetIsBeingUsedException();
        }
    }

    private void softDeletePolicy(
            List<AbacPolicyItem> policyItems,
            List<String> existingResourceSetKeys,
            String deletedDatetime) {

        List<AbacPolicy> policies = policyItems.stream().map(AbacPolicyItem::getPolicy).toList();

        for (AbacPolicy policy : policies) {
            policy.setKey(DELETED + "-" + deletedDatetime + "-" + policy.getKey());
            policy.setDeleted(true);
        }

        abacPolicyRepository.saveAll(policies);

        log.info(
                "Soft deleted policies: {}, given resource sets: {}",
                policies,
                existingResourceSetKeys);
    }

    private void validateAndUpdateResourceSetHasChildren(
            List<ResourceSet> existingResourceSets, boolean cascade) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        var children =
                resourceSetRepository.findByParentInAndProjectAndDeletedFalse(
                        existingResourceSets, projectData);

        var cascadeAndChildrenIsNotEmpty = cascade && !children.isEmpty();

        if (cascadeAndChildrenIsNotEmpty) {
            var parentKeys = existingResourceSets.stream().map(ResourceSet::getKey).toList();
            detachResourceSetChildren(children, parentKeys);
        } else if (!children.isEmpty()) {
            throw new ResourceSetIsBeingUsedException();
        }
    }

    private void detachResourceSetChildren(List<ResourceSet> children, List<String> parentKeys) {

        Set<String> childrenKeySet = new HashSet<>();
        for (var child : children) {
            childrenKeySet.add(child.getKey());
            child.setParent(null);
        }

        resourceSetRepository.saveAll(children);

        log.info(
                "Detached Resource Set Children: {}, Given resource set parents: {}",
                childrenKeySet,
                parentKeys);
    }

    private void softDeleteResourceSets(
            List<String> existingResourceSetKeys,
            List<ResourceSet> resourceSets,
            String deletedDatetime) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        for (var resourceSet : resourceSets) {
            resourceSet.setDeleted(true);
            resourceSet.setKey(DELETED + "-" + deletedDatetime + "-" + resourceSet.getKey());
        }

        resourceSetRepository.saveAll(resourceSets);

        log.info(
                "Successfully Deleted Resource Sets: {} from Project: {}",
                existingResourceSetKeys,
                projectData.getKey());
    }

    private ResourceSet saveResourceSet(
            ResourceSetDto resourceSetDto,
            ResourceSet parent,
            List<ConditionDto> conditionSetDtoList,
            List<String> requestedResourceActionList,
            Project project) {
        ResourceSet resourceSet = resourceSetMapper.toResourceSet(resourceSetDto, parent);
        resourceSet.setProject(project);
        String authorKey = authorRequestScope.getMemberKey();
        resourceSet.setCreatedBy(authorKey);
        resourceSet.setUpdatedBy(authorKey);
        resourceSet = resourceSetRepository.save(resourceSet);

        var conditionGroup = saveConditionGroups(resourceSet, conditionSetDtoList, project);
        resourceSet.setConditionGroup(conditionGroup);
        var actions = saveActions(resourceSet, requestedResourceActionList);
        resourceSet.setActions(actions);

        return resourceSet;
    }

    private List<ResourceSetCondition> saveConditionGroups(
            ResourceSet resourceSetEntity,
            List<ConditionDto> conditionSetDtoList,
            Project project) {
        List<ResourceSetCondition> res = new LinkedList<>();
        for (var cond : conditionSetDtoList) {
            ResourceSetCondition resourceSetCondition = new ResourceSetCondition();
            resourceSetCondition.setResourceSet(resourceSetEntity);
            resourceSetCondition.setOperator(cond.operator());

            resourceSetCondition = resourceSetConditionRepository.save(resourceSetCondition);

            ResourceSetOperand left =
                    getOrCreateOperand(
                            resourceSetCondition, cond.left(), OperandPosition.LEFT, project);
            ResourceSetOperand right =
                    getOrCreateOperand(
                            resourceSetCondition, cond.right(), OperandPosition.RIGHT, project);

            List<ResourceSetOperand> operands = List.of(left, right);
            resourceSetCondition.setOperands(operands);

            res.add(resourceSetCondition);
        }

        return res;
    }

    private List<ResourceSetAction> saveActions(ResourceSet resourceSetEntity, List<String> names) {
        List<ResourceSetAction> entities =
                names.stream()
                        .map(a -> resourceSetActionMapper.toResourceSetAction(resourceSetEntity, a))
                        .collect(Collectors.toList());

        entities = resourceSetActionRepository.saveAll(entities);
        return entities;
    }

    private ResourceSetOperand getOrCreateOperand(
            ResourceSetCondition condition,
            OperandDto operandDto,
            OperandPosition operandPosition,
            Project project) {
        ConditionOperand conditionOperand =
                attributeService.getOrCreateOperand(
                        OperandSubject.RESOURCE_SET,
                        operandDto.operand(),
                        operandDto.type(),
                        operandDto.dataType(),
                        project);

        ResourceSetOperand operand = new ResourceSetOperand();
        operand.setPosition(operandPosition);
        operand.setConditionOperand(conditionOperand);
        operand.setResourceSetCondition(condition);

        operand = resourceSetOperandRepository.save(operand);

        return operand;
    }

    private void validateCreateBulkResourceSet(
            List<ResourceSetDto> resourceSetDtoList, Project projectData) {

        var requestedResourceSetKeyList =
                resourceSetDtoList.stream().map(ResourceSetDto::key).toList();

        var resourceSetEntities =
                resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(
                        requestedResourceSetKeyList, projectData);

        var existingResourceSetKeyList =
                resourceSetEntities.stream()
                        .map(ResourceSet::getKey)
                        .filter(requestedResourceSetKeyList::contains)
                        .toList();

        if (!existingResourceSetKeyList.isEmpty()) {
            throw new ResourceSetAlreadyExist(existingResourceSetKeyList);
        }

        checkingParentResourceSet(resourceSetDtoList, projectData);
    }

    private void validateExistingCreateBulkResourceSet(
            List<ResourceSetDto> newDtos, Project project) {
        int newResourceSetCount = newDtos.size();
        int existingResourceSetCount = resourceSetRepository.countByProjectAndDeletedFalse(project);
        int totalResourceSetCount = newResourceSetCount + existingResourceSetCount;
        if (totalResourceSetCount > MAX_500_SIZE) {
            throw new ResourceSetTooManyException(existingResourceSetCount, newResourceSetCount);
        }
    }

    private void checkingParentResourceSet(
            List<ResourceSetDto> requestedResourceSetKeys, Project projectData) {

        var requestedParentUserSets =
                requestedResourceSetKeys.stream()
                        .map(ResourceSetDto::parent)
                        .map(StringUtils::trim)
                        .filter(org.springframework.util.StringUtils::hasLength)
                        .toList();

        if (!requestedParentUserSets.isEmpty()) {

            var getParentResourceSets =
                    resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(
                            requestedParentUserSets, projectData);

            if (getParentResourceSets.isEmpty()) {
                throw new ParentResourceSetNotFoundException(requestedParentUserSets);
            }

            var queriedParentResourceSets =
                    getParentResourceSets.stream().map(ResourceSet::getKey).toList();

            var notFoundKeys =
                    requestedParentUserSets.stream()
                            .filter(key -> !queriedParentResourceSets.contains(key))
                            .toList();

            // Checking if there is any not found resource set
            if (!notFoundKeys.isEmpty()) {
                throw new ParentResourceSetNotFoundException(notFoundKeys);
            }
        }
    }

    private ResourceSet mappingAndSaveNewResourceSet(
            ResourceSetDto resourceSetDto, Project projectData) {

        ResourceSet parent = null;

        if (StringUtils.isNotEmpty(resourceSetDto.parent())) {
            parent =
                    resourceSetRepository
                            .findByKeyAndProjectAndDeletedFalse(
                                    resourceSetDto.parent(), projectData)
                            .orElseThrow(ResourceSetNotFoundException::new);
        }

        return saveResourceSet(
                resourceSetDto,
                parent,
                resourceSetDto.conditionGroup(),
                resourceSetDto.actions(),
                projectData);
    }

    private void validateResourceSetNotFound(
            List<String> requestedResourceSetKeyList, List<String> existingResourceSetKeyList) {

        var nonExistingResourceSetKeyList =
                requestedResourceSetKeyList.stream()
                        .filter(key -> !existingResourceSetKeyList.contains(key))
                        .toList();

        if (!nonExistingResourceSetKeyList.isEmpty()) {
            throw new ResourceSetNotFoundException(nonExistingResourceSetKeyList);
        }
    }

    private void deleteResourceSetConditions(ResourceSet resourceSetEntity) {
        Project projectData = getProject();

        // Extract condition operands for opportunistic deletion of literals
        List<ConditionOperand> conditionOperands = new ArrayList<>();
        for (var resourceCondition : resourceSetEntity.getConditionGroup()) {
            for (var operand : resourceCondition.getOperands()) {
                conditionOperands.add(operand.getConditionOperand());
            }
            resourceSetOperandRepository.deleteAll(resourceCondition.getOperands());
        }

        resourceSetConditionRepository.deleteAll(resourceSetEntity.getConditionGroup());
        resourceSetEntity.getConditionGroup().clear();

        // Opportunistically delete literals that are no longer in use
        attributeService.opportunisticDeleteLiterals(
                conditionOperands, OperandSubject.RESOURCE_SET, projectData);
    }

    private ResourceSetCondition saveUpdatedResourceSetCondition(
            ConditionDto dto, ResourceSet resourceSetEntity, Project project) {

        var condition = new ResourceSetCondition();
        condition.setResourceSet(resourceSetEntity);
        condition.setOperator(dto.operator());
        condition = resourceSetConditionRepository.save(condition);

        ResourceSetOperand left =
                getOrCreateOperand(condition, dto.left(), OperandPosition.LEFT, project);
        ResourceSetOperand right =
                getOrCreateOperand(condition, dto.right(), OperandPosition.RIGHT, project);
        condition.setOperands(new ArrayList<>(List.of(left, right)));

        condition = resourceSetConditionRepository.save(condition);

        return condition;
    }

    private ResourceSetDto saveUpdatedResourceSet(
            UpdateBulkResourceSetDto updateBulkResourceSetDto,
            ResourceSet resourceSetEntity,
            Project projectData) {

        var requestedResourceActionList = updateBulkResourceSetDto.actions();

        mergeResourceSetActions(requestedResourceActionList, resourceSetEntity, projectData);

        if (resourceSetEntity.getParent() == null) {
            return resourceSetMapper.toResourceSetDto(updateBulkResourceSetDto, null);
        }

        return resourceSetMapper.toResourceSetDto(
                updateBulkResourceSetDto, resourceSetEntity.getParent().getKey());
    }

    private void mergeResourceSetActions(
            List<String> requestedActionNames, ResourceSet resourceSetEntity, Project projectData) {

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        List<ResourceSetAction> allExisting =
                resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSetEntity);

        Set<String> requestedNameSet = new HashSet<>(requestedActionNames);

        // Active actions to remove: currently live but not in the requested list
        List<ResourceSetAction> toRemove =
                allExisting.stream()
                        .filter(a -> !requestedNameSet.contains(a.getActionName()))
                        .toList();

        if (!toRemove.isEmpty()) {
            if (abacPolicyItemRepository
                    .existsByResourceSetActionInAndDeletedFalseAndPolicyProjectAndPolicyDeletedFalse(
                            toRemove, projectData)) {
                throw new ResourceSetActionIsBeingUsedException();
            }
            toRemove.forEach(
                    a -> {
                        a.setActionName(DELETED + "-" + deletedDatetime + "-" + a.getActionName());
                        a.setDeleted(true);
                    });
            resourceSetActionRepository.saveAll(toRemove);
        }

        // Current active names after removals
        Set<String> activeNames =
                allExisting.stream()
                        .filter(a -> !a.isDeleted())
                        .map(ResourceSetAction::getActionName)
                        .collect(Collectors.toSet());

        // Create new rows for names not currently active (deleted names were renamed so they won't
        // block re-creation)
        List<ResourceSetAction> toCreate =
                requestedActionNames.stream()
                        .filter(name -> !activeNames.contains(name))
                        .map(
                                name ->
                                        resourceSetActionMapper.toResourceSetAction(
                                                resourceSetEntity, name))
                        .toList();

        if (!toCreate.isEmpty()) {
            resourceSetActionRepository.saveAll(toCreate);
        }

        resourceSetRepository.save(resourceSetEntity);
    }

    private static String escapeLike(String input) {
        if (input == null) return null;
        return input.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private Project getProject() {
        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }
        return projectData;
    }

    private ProjectDto getProjectDto() {
        var project = getProject();
        return projectMapper.toProjectDto(project);
    }
}
