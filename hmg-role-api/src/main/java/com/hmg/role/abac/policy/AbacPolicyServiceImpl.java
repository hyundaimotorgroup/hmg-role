package com.hmg.role.abac.policy;

import static com.hmg.role.util.Constants.DELETED;
import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;

import com.hmg.role.abac.permission.dto.AbacPolicySearchDto;
import com.hmg.role.abac.policy.dto.AbacPolicyDto;
import com.hmg.role.abac.policy.dto.DeleteBulkAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateBulkAbacPolicyDto;
import com.hmg.role.abac.policy.interfaces.AbacPolicyService;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemMapper;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemRepository;
import com.hmg.role.abac.resourceset.ResourceSet;
import com.hmg.role.abac.resourceset.ResourceSetRepository;
import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.resourceset.action.ResourceSetActionRepository;
import com.hmg.role.abac.resourceset.exceptions.ResourceSetNotFoundException;
import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.abac.scope.AbacScopeRepository;
import com.hmg.role.abac.userset.UserSet;
import com.hmg.role.abac.userset.UserSetRepository;
import com.hmg.role.abac.userset.exceptions.UserSetNotFoundException;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.rbac.policy.exceptions.ActionNotFoundException;
import com.hmg.role.rbac.policy.exceptions.PolicyAlreadyExistException;
import com.hmg.role.rbac.policy.exceptions.PolicyNotFoundException;
import com.hmg.role.rbac.policy.projections.PolicyItemProjection;
import com.hmg.role.rbac.scope.exceptions.ScopeNotFoundException;
import com.hmg.role.rbac.scope.interfaces.ScopeService;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.Cache;
import com.hmg.role.util.dto.ListResponseDto;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class AbacPolicyServiceImpl implements AbacPolicyService {

    private final AbacPolicyRepository policyRepository;
    private final AbacPolicyItemRepository policyItemRepository;
    private final ResourceSetActionRepository resourceSetActionRepository;
    private final UserSetRepository userSetRepository;
    private final ResourceSetRepository resourceSetRepository;
    private final AbacScopeRepository scopeRepository;

    private final AbacPolicyMapper abacPolicyMapper;
    private final AbacPolicyItemMapper abacPolicyItemMapper;

    private final ScopeService scopeService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    public AbacPolicyServiceImpl(
            // necessary to distinguish between the impl beans
            // required because the business logic is ridiculous
            // TODO refactor these when we have a time
            AbacPolicyRepository policyRepository,
            AbacPolicyItemRepository policyItemRepository,
            ResourceSetActionRepository resourceSetActionRepository,
            UserSetRepository userSetRepository,
            ResourceSetRepository resourceSetRepository,
            AbacScopeRepository scopeRepository,
            AbacPolicyMapper abacPolicyMapper,
            AbacPolicyItemMapper abacPolicyItemMapper,
            @Qualifier("abacScopeServiceImpl") ScopeService scopeService) {
        this.policyRepository = policyRepository;
        this.policyItemRepository = policyItemRepository;
        this.resourceSetActionRepository = resourceSetActionRepository;
        this.userSetRepository = userSetRepository;
        this.resourceSetRepository = resourceSetRepository;
        this.scopeRepository = scopeRepository;
        this.abacPolicyMapper = abacPolicyMapper;
        this.abacPolicyItemMapper = abacPolicyItemMapper;
        this.scopeService = scopeService;
    }

    @Override
    public AbacPolicyDto createPolicy(AbacPolicyDto abacPolicyDto) {

        var abacPolicyDtoList = List.of(abacPolicyDto);
        return createBulkPolicyProcess(abacPolicyDtoList).getFirst();
    }

    @Override
    public ListResponseDto<AbacPolicyDto> createBulkPolicies(List<AbacPolicyDto> abacPolicyDtos) {

        var createBulkPolicyResult = createBulkPolicyProcess(abacPolicyDtos);

        return ListResponseDto.create(createBulkPolicyResult);
    }

    private List<AbacPolicyDto> createBulkPolicyProcess(List<AbacPolicyDto> abacPolicyDtos) {

        Project projectData = getProject();

        var policiesScope =
                abacPolicyDtos.stream()
                        .map(AbacPolicyDto::scope)
                        .filter(StringUtils::isNoneBlank)
                        .toList();

        scopeService.validateScopes(policiesScope, projectData);

        var requestedResourceSetList =
                abacPolicyDtos.stream().map(AbacPolicyDto::resourceSet).distinct().toList();

        var resourceSetEntities =
                resourceSetRepository.findByKeyInAndProjectAndDeletedFalse(
                        requestedResourceSetList, projectData);

        var resourceSetEntityMap =
                resourceSetEntities.stream()
                        .collect(Collectors.toMap(ResourceSet::getKey, resourceSet -> resourceSet));

        var policyKeys = abacPolicyDtos.stream().map(AbacPolicyDto::key).distinct().toList();

        var validateCreateBulkPoliciesDatas =
                new ValidateCreateBulkPoliciesData(
                        requestedResourceSetList, resourceSetEntities, policyKeys, projectData);

        validateCreateBulkPolicy(validateCreateBulkPoliciesDatas);

        var bulkCreateData =
                new BulkCreateAbacPoliciesData(abacPolicyDtos, resourceSetEntityMap, projectData);

        var saved = saveBulkPolicies(bulkCreateData);
        var savedDtos =
                saved.entrySet().stream()
                        .map(k -> abacPolicyMapper.toPolicyDto(k.getKey(), k.getValue()))
                        .toList();

        log.info(
                "Successfully Created Policies: {} from Project: {}",
                policyKeys,
                projectData.getKey());

        return savedDtos;
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

    @Override
    public ListResponseDto<AbacPolicyDto> getPolicies(AbacPolicySearchDto searchDto) {

        // overriding blank filter to null to get all/unfiltered result
        var userSetKeyLike =
                StringUtils.isBlank(searchDto.getUserSetKeyLike())
                        ? null
                        : escapeLike(searchDto.getUserSetKeyLike());
        var resourceSetKeyLike =
                StringUtils.isBlank(searchDto.getResourceSetKeyLike())
                        ? null
                        : escapeLike(searchDto.getResourceSetKeyLike());

        Project project = getProject();

        var pageable = searchDto.pageRequest();

        String scopeKey = searchDto.getScopeKey();
        AbacScope scope =
                scopeRepository
                        .findByKeyAndProjectAndDeletedFalse(scopeKey, project)
                        .orElseThrow(() -> new ScopeNotFoundException(scopeKey));

        // this is ugly
        // but necessary to avoid N+1
        // since we're not doing native queries
        long startTimeMillis = System.currentTimeMillis();

        var policyQueryResults =
                policyItemRepository.findIdsByCriteria(
                        userSetKeyLike, resourceSetKeyLike, project, scope, pageable);
        var policyItemIds = parseItemIds(policyQueryResults);
        var policyItems = policyItemRepository.findByIdIn(policyItemIds);

        log.debug(
                "Querying: {}, processing time: {} ms",
                searchDto,
                (System.currentTimeMillis() - startTimeMillis));

        pageable = policyQueryResults.getPageable();

        Map<AbacPolicy, List<AbacPolicyItem>> policies = mapPoliciesFromItems(policyItems);
        var policyDtos =
                policies.entrySet().stream()
                        .map(k -> abacPolicyMapper.toPolicyDto(k.getKey(), k.getValue()))
                        .toList();
        var page = new PageImpl<>(policyDtos, pageable, policyQueryResults.getTotalElements());

        return ListResponseDto.create(page);
    }

    @Override
    public AbacPolicyDto getPolicyByKey(String policyKey) {

        Project project = getProject();

        var policy =
                policyRepository
                        .findByKeyAndProjectAndDeletedFalse(policyKey, project)
                        .orElseThrow(PolicyNotFoundException::new);

        var policyItems = policyItemRepository.findByPolicyAndDeletedFalse(policy);

        if (policyItems.isEmpty()) {
            throw new PolicyNotFoundException(policyKey);
        }

        return abacPolicyMapper.toPolicyDto(policyItems);
    }

    @Override
    public AbacPolicyDto updatePolicy(String policyKey, UpdateAbacPolicyDto updateAbacPolicyDto) {

        var requestedResourceSet = updateAbacPolicyDto.resourceSet();

        var requestedUserSetList = updateAbacPolicyDto.userSets();

        Project projectData = getProject();

        String scopeKey = updateAbacPolicyDto.scope();
        scopeService.validateScope(scopeKey, projectData);

        var policyEntity =
                policyRepository
                        .findByKeyAndProjectAndDeletedFalse(policyKey, projectData)
                        .orElseThrow(PolicyNotFoundException::new);

        var resourceSetEntity =
                resourceSetRepository
                        .findByKeyAndProjectAndDeletedFalse(requestedResourceSet, projectData)
                        .orElseThrow(ResourceSetNotFoundException::new);

        var resourceSetActionEntities =
                resourceSetActionRepository.findByResourceSetAndDeletedFalse(resourceSetEntity);

        var userSetEntities =
                userSetRepository.findByKeyInAndProjectAndDeletedFalse(
                        requestedUserSetList, projectData);

        var validateUpdatePolicyData =
                new ValidateUpdatePolicyItemData(
                        policyKey,
                        updateAbacPolicyDto,
                        resourceSetEntity,
                        resourceSetActionEntities,
                        userSetEntities);

        validateUpdatePolicyItem(validateUpdatePolicyData);

        var actionMap = mappingResourceSetAction(resourceSetActionEntities);

        var scopeEntity =
                scopeRepository
                        .findByKeyAndProjectAndDeletedFalse(scopeKey, projectData)
                        .orElseThrow(() -> new ScopeNotFoundException(scopeKey));

        var memberKey = authorRequestScope.getMemberKey();
        abacPolicyMapper.toPolicy(policyEntity, updateAbacPolicyDto, memberKey);

        var mergeData =
                new PolicyItemMergeData(
                        policyEntity,
                        scopeEntity,
                        requestedUserSetList,
                        updateAbacPolicyDto.actions(),
                        updateAbacPolicyDto.effect(),
                        actionMap,
                        projectData);

        var activeItems = mergePolicyItems(mergeData);

        AbacPolicy updatedPolicy = policyRepository.save(policyEntity);

        log.info(
                "Successfully Updated Policy: {} from Project: {}",
                policyKey,
                projectData.getKey());

        return abacPolicyMapper.toPolicyDto(updatedPolicy, activeItems);
    }

    public ListResponseDto<AbacPolicyDto> updateBulkPolicies(
            List<UpdateBulkAbacPolicyDto> updateBulkAbacPolicyDtos) {

        Project projectData = getProject();

        var policiesScope =
                updateBulkAbacPolicyDtos.stream()
                        .map(UpdateBulkAbacPolicyDto::scope)
                        .filter(StringUtils::isNoneBlank)
                        .toList();

        scopeService.validateScopes(policiesScope, projectData);

        var requestedPolicyItemKeyList =
                updateBulkAbacPolicyDtos.stream().map(UpdateBulkAbacPolicyDto::key).toList();

        var policyEntities =
                policyRepository.findByKeyInAndProjectAndDeletedFalse(
                        requestedPolicyItemKeyList, projectData);

        var existingPolicyKeyList = policyEntities.stream().map(AbacPolicy::getKey).toList();

        validateBulkPolicyKeysNotFound(requestedPolicyItemKeyList, existingPolicyKeyList);

        var policyEntityMap = mappingAbacPolicyEntitiesProcess(policyEntities);

        List<AbacPolicyDto> policyDtos = new ArrayList<>();

        var bulkUpdateData =
                new BulkUpdateAbacPoliciesData(
                        policyEntityMap, projectData, updateBulkAbacPolicyDtos);

        for (var updateBulkAbacPolicyDto : updateBulkAbacPolicyDtos) {
            var policyDto = saveAbacPolicyProcess(updateBulkAbacPolicyDto, bulkUpdateData);
            policyDtos.add(policyDto);
        }

        log.info(
                "Successfully Updated Policies: {} from Project: {}",
                existingPolicyKeyList,
                projectData.getKey());

        return ListResponseDto.create(policyDtos);
    }

    public void deletePolicy(String policyKey) {
        deletePolicyItemProcess(List.of(policyKey));
    }

    public void deleteBulkPolicies(DeleteBulkAbacPolicyDto deleteBulkAbacPolicyDto) {

        var requestedPolicyKeyList = deleteBulkAbacPolicyDto.keys();

        deletePolicyItemProcess(requestedPolicyKeyList);
    }

    private void validatePolicyItem(
            AbacPolicyDto abacPolicyDto,
            List<ResourceSetAction> resourceSetActionDatas,
            List<UserSet> userSetEntities) {

        var userSets = abacPolicyDto.userSets();

        var resourceSetActions = abacPolicyDto.actions();

        checkResourceAction(resourceSetActionDatas, resourceSetActions);

        checkUserSet(userSetEntities, userSets);

        // nested scope validations are no longer required since nested scope feature is dropped
    }

    // TODO: Need To Refactor into one method process. since it has warning in editor/IDE
    private void validateUpdatePolicyItem(
            ValidateUpdatePolicyItemData validateUpdatePolicyItemData) {

        var userSets = validateUpdatePolicyItemData.updateAbacPolicyDto.userSets();
        var resourceSetActions = validateUpdatePolicyItemData.updateAbacPolicyDto.actions();
        var resourceSetActionEntities = validateUpdatePolicyItemData.resourceSetActionEntities();
        var userSetEntities = validateUpdatePolicyItemData.userSetEntities();

        checkResourceAction(resourceSetActionEntities, resourceSetActions);
        checkUserSet(userSetEntities, userSets);

        // nested scope validations are no longer required since nested scope feature is dropped
    }

    private void validatePolicyItem(
            UpdateBulkAbacPolicyDto updateBulkAbacPolicyDto,
            List<ResourceSetAction> resourceSetActionEntities,
            List<UserSet> userSetEntities) {

        var userSets = updateBulkAbacPolicyDto.userSets();

        var resourceSetActions = updateBulkAbacPolicyDto.actions();

        checkResourceAction(resourceSetActionEntities, resourceSetActions);

        checkUserSet(userSetEntities, userSets);
    }

    private void checkResourceAction(
            List<ResourceSetAction> resourceSetEntities, List<String> actions) {

        var resourceActionNames =
                resourceSetEntities.stream().map(ResourceSetAction::getActionName).toList();

        for (String action : actions) {

            if (!resourceActionNames.contains(action)) {
                throw new ActionNotFoundException(action);
            }
        }
    }

    private void checkUserSet(List<UserSet> userSetEntities, List<String> userSetKeys) {

        var userSetNames = userSetEntities.stream().map(UserSet::getKey).toList();

        for (String userSet : userSetKeys) {

            if (!userSetNames.contains(userSet)) {
                throw new UserSetNotFoundException(userSet);
            }
        }
    }

    private void validateCreateBulkPolicy(
            ValidateCreateBulkPoliciesData validateCreateBulkPoliciesData) {

        var resourceSetEntities = validateCreateBulkPoliciesData.resourceSetEntities();

        var requestedResourceSetList = validateCreateBulkPoliciesData.requestedResourceSetList();

        var policyKeys = validateCreateBulkPoliciesData.policyKeys();

        var projectData = validateCreateBulkPoliciesData.project();

        validateResourceSetList(resourceSetEntities, requestedResourceSetList);

        var policies =
                policyRepository.findByKeyInAndProjectAndDeletedFalse(policyKeys, projectData);

        var existingPolicies =
                policies.stream().map(AbacPolicy::getKey).filter(policyKeys::contains).toList();

        if (!existingPolicies.isEmpty()) {
            throw new PolicyAlreadyExistException(existingPolicies);
        }
    }

    private void validateResourceSetList(
            List<ResourceSet> resourceSetEntities, List<String> requestedResourceSetList) {
        var existingResourceSetKeys =
                resourceSetEntities.stream().map(ResourceSet::getKey).toList();

        var nonExistingResourceSetKeys =
                requestedResourceSetList.stream()
                        .filter(resourceSet -> !existingResourceSetKeys.contains(resourceSet))
                        .toList();

        if (!nonExistingResourceSetKeys.isEmpty()) {
            throw new ResourceSetNotFoundException(nonExistingResourceSetKeys);
        }
    }

    private Map<AbacPolicy, List<AbacPolicyItem>> saveBulkPolicies(
            BulkCreateAbacPoliciesData data) {

        var abacPolicyDtoList = data.abacPolicyDtos;
        var projectData = data.projectData;

        Map<AbacPolicy, List<AbacPolicyItem>> policyEntities = new HashMap<>();
        for (var abacPolicyDto : abacPolicyDtoList) {
            var requestedResourceSetDto = abacPolicyDto.resourceSet();
            var requestedUserSetDtoList = abacPolicyDto.userSets();

            var resourceSetEntity = data.resourceSetEntityMap.get(requestedResourceSetDto);

            var resourceActionEntities =
                    data.resourceSetActionsByResourceSet.get().get(resourceSetEntity);

            var userSetEntities =
                    requestedUserSetDtoList.stream()
                            .map(key -> data.userSetByKey.get().get(key))
                            .filter(Objects::nonNull)
                            .toList();

            validatePolicyItem(abacPolicyDto, resourceActionEntities, userSetEntities);

            Map<String, ResourceSetAction> actionMap =
                    mappingResourceSetAction(resourceActionEntities);

            var scopeKey = abacPolicyDto.scope();
            var scopeEntity = data.scopeByKey.get().get(scopeKey);
            if (scopeEntity == null) {
                throw new ScopeNotFoundException(scopeKey);
            }

            validateNoDuplicatePolicyItems(
                    null,
                    List.of(resourceSetEntity),
                    abacPolicyDto.actions(),
                    userSetEntities,
                    projectData,
                    scopeKey);

            var memberKey = authorRequestScope.getMemberKey();
            var policy =
                    abacPolicyMapper.toPolicy(abacPolicyDto, projectData, memberKey, memberKey);

            AbacPolicy savedPolicy = policyRepository.save(policy);

            var mappingPolicyItemListData =
                    new MappingPolicyItemListData(
                            savedPolicy,
                            abacPolicyDto,
                            projectData,
                            actionMap,
                            data.userSetByKey.get());

            List<AbacPolicyItem> policyItems =
                    mappingPolicyItemListProcess(mappingPolicyItemListData, scopeEntity);

            policyItems = policyItemRepository.saveAll(policyItems);
            policyEntities.put(policy, policyItems);
        }

        return policyEntities;
    }

    private List<AbacPolicyItem> mappingPolicyItemListProcess(
            MappingPolicyItemListData mappingPolicyItemListData, AbacScope scope) {

        var abacPolicyDto = mappingPolicyItemListData.abacPolicyDto();

        List<AbacPolicyItem> policyItems = new ArrayList<>();

        abacPolicyDto.userSets().stream()
                .map(
                        userSet -> {
                            var entity = mappingPolicyItemListData.userSetByKey().get(userSet);
                            if (entity == null) {
                                throw new UserSetNotFoundException(userSet);
                            }
                            return entity;
                        })
                .flatMap(
                        userSetData ->
                                mappingPolicyItemProcess(
                                        mappingPolicyItemListData, userSetData, scope))
                .forEach(policyItems::add);

        return policyItems;
    }

    private Stream<AbacPolicyItem> mappingPolicyItemProcess(
            MappingPolicyItemListData mappingPolicyItemListData,
            UserSet userSetData,
            AbacScope scope) {

        var abacPolicyDto = mappingPolicyItemListData.abacPolicyDto();

        var actionMap = mappingPolicyItemListData.actionMap();

        var savedPolicy = mappingPolicyItemListData.savedPolicy();

        return abacPolicyDto.actions().stream()
                .filter(actionMap::containsKey)
                .map(actionMap::get)
                .map(
                        action ->
                                abacPolicyItemMapper.toPolicyItem(
                                        abacPolicyDto, action, scope, savedPolicy, userSetData));
    }

    private Map<String, ResourceSetAction> mappingResourceSetAction(
            List<ResourceSetAction> resourceSetActionList) {

        return resourceSetActionList.stream()
                .collect(
                        Collectors.toMap(
                                ResourceSetAction::getActionName, // Key mapper
                                Function.identity(), // Value mapper
                                (existing, replacement) ->
                                        existing // Merge function to handle duplicates
                                ));
    }

    private AbacPolicyDto saveAbacPolicyProcess(
            UpdateBulkAbacPolicyDto updateBulkAbacPolicyDto, BulkUpdateAbacPoliciesData data) {

        var policyEntityMap = data.policyEntityMap;
        var projectData = data.projectData;

        var policy = policyEntityMap.get(updateBulkAbacPolicyDto.key());
        var resourceSetDto = updateBulkAbacPolicyDto.resourceSet();

        var resourceSet = data.resourceSetByKey.get().get(resourceSetDto);
        if (resourceSet == null) {
            throw new ResourceSetNotFoundException();
        }

        var resourceSetActionEntities = data.resourceSetActionsByResourceSet.get().get(resourceSet);

        var userSets = updateBulkAbacPolicyDto.userSets();
        var userSetEntities =
                userSets.stream()
                        .map(key -> data.userSetByKey.get().get(key))
                        .filter(Objects::nonNull)
                        .toList();

        validatePolicyItem(updateBulkAbacPolicyDto, resourceSetActionEntities, userSetEntities);

        Map<String, ResourceSetAction> actionMap =
                mappingResourceSetAction(resourceSetActionEntities);

        var scopeKey = updateBulkAbacPolicyDto.scope();
        var scope = data.scopeByKey.get().get(scopeKey);
        if (scope == null) {
            throw new ScopeNotFoundException(scopeKey);
        }

        var memberKey = authorRequestScope.getMemberKey();
        abacPolicyMapper.toPolicy(policy, updateBulkAbacPolicyDto, memberKey);

        var mergeData =
                new PolicyItemMergeData(
                        policy,
                        scope,
                        updateBulkAbacPolicyDto.userSets(),
                        updateBulkAbacPolicyDto.actions(),
                        updateBulkAbacPolicyDto.effect(),
                        actionMap,
                        projectData);

        List<AbacPolicyItem> activeItems = mergePolicyItems(mergeData);

        AbacPolicy updatedPolicy = policyRepository.save(policy);

        return abacPolicyMapper.toPolicyDto(updatedPolicy, activeItems);
    }

    private void validateBulkPolicyKeysNotFound(
            List<String> requestedPolicyItemKeyList, List<String> existingPolicyKeyList) {
        var policyEntityKeyNotFound =
                requestedPolicyItemKeyList.stream()
                        .filter(key -> !existingPolicyKeyList.contains(key))
                        .toList();

        if (!policyEntityKeyNotFound.isEmpty()) {
            throw new PolicyNotFoundException(policyEntityKeyNotFound);
        }
    }

    private Map<String, AbacPolicy> mappingAbacPolicyEntitiesProcess(
            List<AbacPolicy> policyEntities) {
        return policyEntities.stream()
                .collect(Collectors.toMap(AbacPolicy::getKey, abacPolicy -> abacPolicy));
    }

    private void deletePolicyItemProcess(List<String> requestedPolicyKeyList) {

        Project projectData = getProject();

        var policyEntityList =
                policyRepository.findByKeyInAndProjectAndDeletedFalse(
                        requestedPolicyKeyList, projectData);

        var existingPolicyKeyList = policyEntityList.stream().map(AbacPolicy::getKey).toList();

        validateBulkPolicyKeysNotFound(requestedPolicyKeyList, existingPolicyKeyList);

        var mappedPolicy = mappingAbacPolicyEntitiesProcess(policyEntityList);

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        List<AbacPolicy> updatedPolicies = new ArrayList<>();

        for (var existingPolicyKey : existingPolicyKeyList) {
            var policy = mappedPolicy.get(existingPolicyKey);

            policy.setDeleted(true);
            policy.setKey(DELETED + "-" + deletedDatetime + "-" + policy.getKey());

            updatedPolicies.add(policy);
        }

        updatedPolicies = policyRepository.saveAll(updatedPolicies);

        // delete related policy items
        List<AbacPolicyItem> items =
                policyItemRepository.findByPolicyInAndDeletedFalse(updatedPolicies);
        items.forEach(e -> e.setDeleted(true));
        policyItemRepository.saveAll(items);

        log.info(
                "Successfully Deleted Policies: {} from Project: {}",
                existingPolicyKeyList,
                projectData.getKey());
    }

    private static Map<AbacPolicy, List<AbacPolicyItem>> mapPoliciesFromItems(
            List<AbacPolicyItem> items) {
        return items.stream().collect(Collectors.groupingBy(AbacPolicyItem::getPolicy));
    }

    private static List<Long> parseItemIds(Page<PolicyItemProjection> res) {
        if (res != null && !res.isEmpty()) {
            return res.stream()
                    .map(PolicyItemProjection::getPolicyItemIdsCsv)
                    .flatMap(r -> Arrays.stream(r.split(",")).map(Long::valueOf))
                    .toList();
        } else {
            return List.of();
        }
    }

    // per-request cache for bulk create — lifetime is one createBulkPolicies call
    private class BulkCreateAbacPoliciesData {

        final List<AbacPolicyDto> abacPolicyDtos;
        final Map<String, ResourceSet> resourceSetEntityMap;
        final Project projectData;
        final Cache<Map<ResourceSet, List<ResourceSetAction>>> resourceSetActionsByResourceSet;
        final Cache<Map<String, UserSet>> userSetByKey;
        final Cache<Map<String, AbacScope>> scopeByKey;

        BulkCreateAbacPoliciesData(
                List<AbacPolicyDto> abacPolicyDtos,
                Map<String, ResourceSet> resourceSetEntityMap,
                Project projectData) {
            this.abacPolicyDtos = abacPolicyDtos;
            this.resourceSetEntityMap = resourceSetEntityMap;
            this.projectData = projectData;
            this.resourceSetActionsByResourceSet =
                    new Cache<>(
                            () ->
                                    resourceSetEntityMap.values().stream()
                                            .collect(
                                                    Collectors.toMap(
                                                            Function.identity(),
                                                            resourceSetActionRepository
                                                                    ::findByResourceSetAndDeletedFalse)));
            this.userSetByKey =
                    new Cache<>(
                            () -> {
                                var allUserSetKeys =
                                        abacPolicyDtos.stream()
                                                .flatMap(dto -> dto.userSets().stream())
                                                .distinct()
                                                .toList();
                                return userSetRepository
                                        .findByKeyInAndProjectAndDeletedFalse(
                                                allUserSetKeys, projectData)
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        UserSet::getKey, Function.identity()));
                            });
            this.scopeByKey =
                    new Cache<>(
                            () -> {
                                var allScopeKeys =
                                        abacPolicyDtos.stream()
                                                .map(AbacPolicyDto::scope)
                                                .filter(StringUtils::isNotBlank)
                                                .distinct()
                                                .toList();
                                return scopeRepository
                                        .findByKeyInAndProjectAndDeletedFalse(
                                                allScopeKeys, projectData)
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        AbacScope::getKey, Function.identity()));
                            });
        }
    }

    // per-request cache for bulk update — lifetime is one updateBulkPolicies call
    private class BulkUpdateAbacPoliciesData {

        final Map<String, AbacPolicy> policyEntityMap;
        final Project projectData;
        final Cache<Map<String, ResourceSet>> resourceSetByKey;
        final Cache<Map<ResourceSet, List<ResourceSetAction>>> resourceSetActionsByResourceSet;
        final Cache<Map<String, UserSet>> userSetByKey;
        final Cache<Map<String, AbacScope>> scopeByKey;

        BulkUpdateAbacPoliciesData(
                Map<String, AbacPolicy> policyEntityMap,
                Project projectData,
                List<UpdateBulkAbacPolicyDto> dtoList) {
            this.policyEntityMap = policyEntityMap;
            this.projectData = projectData;
            this.resourceSetByKey =
                    new Cache<>(
                            () -> {
                                var allResourceSetKeys =
                                        dtoList.stream()
                                                .map(UpdateBulkAbacPolicyDto::resourceSet)
                                                .distinct()
                                                .toList();
                                return resourceSetRepository
                                        .findByKeyInAndProjectAndDeletedFalse(
                                                allResourceSetKeys, projectData)
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        ResourceSet::getKey, Function.identity()));
                            });
            Cache<Map<String, ResourceSet>> rsCache = this.resourceSetByKey;
            this.resourceSetActionsByResourceSet =
                    new Cache<>(
                            () ->
                                    rsCache.get().values().stream()
                                            .collect(
                                                    Collectors.toMap(
                                                            Function.identity(),
                                                            resourceSetActionRepository
                                                                    ::findByResourceSetAndDeletedFalse)));
            this.userSetByKey =
                    new Cache<>(
                            () -> {
                                var allUserSetKeys =
                                        dtoList.stream()
                                                .flatMap(dto -> dto.userSets().stream())
                                                .distinct()
                                                .toList();
                                return userSetRepository
                                        .findByKeyInAndProjectAndDeletedFalse(
                                                allUserSetKeys, projectData)
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        UserSet::getKey, Function.identity()));
                            });
            this.scopeByKey =
                    new Cache<>(
                            () -> {
                                var allScopeKeys =
                                        dtoList.stream()
                                                .map(UpdateBulkAbacPolicyDto::scope)
                                                .filter(StringUtils::isNotBlank)
                                                .distinct()
                                                .toList();
                                return scopeRepository
                                        .findByKeyInAndProjectAndDeletedFalse(
                                                allScopeKeys, projectData)
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        AbacScope::getKey, Function.identity()));
                            });
        }
    }

    private record ValidateUpdatePolicyItemData(
            String policyKey,
            UpdateAbacPolicyDto updateAbacPolicyDto,
            ResourceSet resourceSetEntity,
            List<ResourceSetAction> resourceSetActionEntities,
            List<UserSet> userSetEntities) {}

    private record ValidateCreateBulkPoliciesData(
            List<String> requestedResourceSetList,
            List<ResourceSet> resourceSetEntities,
            List<String> policyKeys,
            Project project) {}

    private record MappingPolicyItemListData(
            AbacPolicy savedPolicy,
            AbacPolicyDto abacPolicyDto,
            Project projectData,
            Map<String, ResourceSetAction> actionMap,
            Map<String, UserSet> userSetByKey) {}

    private record PolicyItemMergeData(
            AbacPolicy policy,
            AbacScope scope,
            List<String> userSetKeys,
            List<String> actionNames,
            Effect effect,
            Map<String, ResourceSetAction> actionMap,
            Project projectData) {}

    private List<AbacPolicyItem> mergePolicyItems(PolicyItemMergeData data) {
        var existingItems = policyItemRepository.findByPolicyAndDeletedFalse(data.policy());

        if (data.actionMap().isEmpty()) {
            existingItems.forEach(item -> item.setDeleted(true));
            policyItemRepository.saveAll(existingItems);
            return List.of();
        }

        String scopeKey = data.scope().getKey();
        String resourceSetKey =
                data.actionMap().values().stream()
                        .findFirst()
                        .map(a -> a.getResourceSet().getKey())
                        .orElseThrow();

        Set<String> requestedKeys = new HashSet<>();
        for (String userSetKey : data.userSetKeys()) {
            for (String actionName : data.actionNames()) {
                if (data.actionMap().containsKey(actionName)) {
                    requestedKeys.add(
                            scopeKey + "|" + userSetKey + "|" + resourceSetKey + "|" + actionName);
                }
            }
        }

        // Update effect on survivors; soft-delete items not in the requested set
        List<AbacPolicyItem> survivingItems = new ArrayList<>();
        existingItems.forEach(
                item -> {
                    String key =
                            item.getScopeKey()
                                    + "|"
                                    + item.getUserSetKey()
                                    + "|"
                                    + item.getResourceSetKey()
                                    + "|"
                                    + item.getActionName();
                    if (requestedKeys.contains(key)) {
                        item.setEffect(data.effect());
                        survivingItems.add(item);
                    } else {
                        item.setDeleted(true);
                    }
                });
        policyItemRepository.saveAll(existingItems);

        Set<String> survivingKeys =
                survivingItems.stream()
                        .map(
                                item ->
                                        item.getScopeKey()
                                                + "|"
                                                + item.getUserSetKey()
                                                + "|"
                                                + item.getResourceSetKey()
                                                + "|"
                                                + item.getActionName())
                        .collect(Collectors.toSet());

        // Batch-load all requested user sets to avoid N+1 queries
        Map<String, UserSet> userSetByKey =
                userSetRepository
                        .findByKeyInAndProjectAndDeletedFalse(
                                data.userSetKeys(), data.projectData())
                        .stream()
                        .collect(Collectors.toMap(UserSet::getKey, Function.identity()));
        for (String userSetKey : data.userSetKeys()) {
            if (!userSetByKey.containsKey(userSetKey)) {
                throw new UserSetNotFoundException(userSetKey);
            }
        }

        // Build new items for combinations not already surviving
        List<AbacPolicyItem> toCreate = new ArrayList<>();
        Set<UserSet> newUserSets = new LinkedHashSet<>();
        Set<String> newActionNames = new LinkedHashSet<>();

        for (String userSetKey : data.userSetKeys()) {
            UserSet userSet = userSetByKey.get(userSetKey);
            for (String actionName : data.actionNames()) {
                String key = scopeKey + "|" + userSetKey + "|" + resourceSetKey + "|" + actionName;
                if (!survivingKeys.contains(key) && data.actionMap().containsKey(actionName)) {
                    ResourceSetAction action = data.actionMap().get(actionName);
                    AbacPolicyItem newItem = new AbacPolicyItem();
                    newItem.setEffect(data.effect());
                    newItem.setResourceSetAction(action);
                    newItem.setScope(data.scope());
                    newItem.setPolicy(data.policy());
                    newItem.setUserSet(userSet);
                    toCreate.add(newItem);
                    newUserSets.add(userSet);
                    newActionNames.add(actionName);
                }
            }
        }

        List<AbacPolicyItem> savedNew = List.of();
        if (!toCreate.isEmpty()) {
            List<ResourceSet> resourceSets =
                    data.actionMap().values().stream()
                            .map(ResourceSetAction::getResourceSet)
                            .distinct()
                            .toList();
            validateNoDuplicatePolicyItems(
                    data.policy(),
                    resourceSets,
                    new ArrayList<>(newActionNames),
                    new ArrayList<>(newUserSets),
                    data.projectData(),
                    scopeKey);
            savedNew = policyItemRepository.saveAll(toCreate);
        }

        List<AbacPolicyItem> allActive = new ArrayList<>(survivingItems);
        allActive.addAll(savedNew);
        return allActive;
    }

    private void validateNoDuplicatePolicyItems(
            AbacPolicy currentPolicy,
            List<ResourceSet> resourceSets,
            List<String> actionNames,
            List<UserSet> userSets,
            Project project,
            String scopeKey) {

        if (userSets.isEmpty() || actionNames.isEmpty()) {
            return;
        }

        Long excludePolicyId = currentPolicy == null ? null : currentPolicy.getId();
        if (policyItemRepository.existsConflictingPolicyItem(
                resourceSets, actionNames, userSets, project, scopeKey, excludePolicyId)) {
            throw new DuplicateAbacPolicyItemException();
        }
    }
}
