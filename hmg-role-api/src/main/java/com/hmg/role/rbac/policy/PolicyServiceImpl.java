package com.hmg.role.rbac.policy;

import static com.hmg.role.util.Constants.DELETED;
import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;

import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.dto.CreatePolicyDto;
import com.hmg.role.rbac.policy.dto.DeleteBulkPolicyDto;
import com.hmg.role.rbac.policy.dto.IPolicyModificationDto;
import com.hmg.role.rbac.policy.dto.PolicyDto;
import com.hmg.role.rbac.policy.dto.PolicySearchDto;
import com.hmg.role.rbac.policy.dto.UpdateBulkPolicyDto;
import com.hmg.role.rbac.policy.dto.UpdatePolicyDto;
import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.rbac.policy.exceptions.ActionNotFoundException;
import com.hmg.role.rbac.policy.exceptions.PolicyAlreadyExistException;
import com.hmg.role.rbac.policy.exceptions.PolicyNotFoundException;
import com.hmg.role.rbac.policy.interfaces.PolicyService;
import com.hmg.role.rbac.policy.policyitem.DuplicatePolicyItemException;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import com.hmg.role.rbac.policy.policyitem.PolicyItemRepository;
import com.hmg.role.rbac.resourceaction.ResourceAction;
import com.hmg.role.rbac.resourceaction.ResourceActionRepository;
import com.hmg.role.rbac.resourcetype.exceptions.ResourceTypeNotFoundException;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.role.interfaces.RoleService;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.scope.interfaces.ScopeService;
import com.hmg.role.sdk.common.util.Utils;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.Cache;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.Metadata;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyItemRepository policyItemRepository;
    private final ResourceActionRepository resourceActionRepository;
    private final RoleService roleService;
    private final ScopeService scopeService;

    private final PolicyMapper policyMapper;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    private AuditService auditService;

    public PolicyServiceImpl(
            // necessary to distinguish between the impl beans
            // required because the business logic is ridiculous
            // TODO refactor these when we have a time
            PolicyRepository policyRepository,
            PolicyItemRepository policyItemRepository,
            ResourceActionRepository resourceActionRepository,
            RoleService roleService,
            PolicyMapper policyMapper,
            @Qualifier("rbacScopeServiceImpl") ScopeService scopeService) {
        this.policyRepository = policyRepository;
        this.policyItemRepository = policyItemRepository;
        this.resourceActionRepository = resourceActionRepository;
        this.roleService = roleService;
        this.policyMapper = policyMapper;
        this.scopeService = scopeService;
    }

    public ListResponseDto<PolicyDto> getAllPolicies(PolicySearchDto policySearchDto) {

        Project project = authorRequestScope.getProject();

        var pageReq = policySearchDto.pageRequest();

        long start = System.currentTimeMillis();

        var policyItemKeys =
                policyItemRepository.findHeadersByOptionalParams(
                        project.getKey(),
                        policySearchDto.getRoleKey(),
                        policySearchDto.getScopeKey(),
                        policySearchDto.getResourceType(),
                        escapeLike(policySearchDto.getAction()),
                        pageReq);

        log.debug("query header: {} ms", (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        List<String> policyItemIds =
                policyItemKeys.stream()
                        .map(p -> p.getPolicyItemIdsCsv().split(","))
                        .flatMap(Arrays::stream)
                        .toList();

        log.debug("header mapping: {} ms", (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        List<PolicyItem> policyItems = policyItemRepository.findByIdIn(policyItemIds);

        log.debug("query items: {} ms", (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        Map<String, List<PolicyItem>> policyItemMapByPolicyKey =
                policyItems.stream().collect(Collectors.groupingBy(PolicyItem::getPolicyKey));

        var results =
                policyItemMapByPolicyKey.entrySet().stream()
                        .map(
                                entry -> {
                                    var policyKey = entry.getKey();
                                    var policyItemList = entry.getValue();
                                    var actionNameList =
                                            policyItemList.stream()
                                                    .map(PolicyItem::getActionName)
                                                    .toList();
                                    var roleKeyList =
                                            policyItemList.stream()
                                                    .map(PolicyItem::getRoleKey)
                                                    .distinct()
                                                    .toList();
                                    var policyItem = entry.getValue().getFirst();
                                    return PolicyDto.builder()
                                            .key(policyKey)
                                            .description(policyItem.getPolicyDescription())
                                            .scopeKey(policyItem.getScopeKey())
                                            .resourceType(policyItem.getResourceTypeKey())
                                            .actions(actionNameList)
                                            .roles(roleKeyList)
                                            .effect(policyItem.getEffect().toString())
                                            .build();
                                })
                        .toList();

        log.debug("response mapping: {} ms", (System.currentTimeMillis() - start));

        return new ListResponseDto<>(results, Metadata.create(policyItemKeys));
    }

    public PolicyDto createPolicy(CreatePolicyDto createPolicyDto) {
        return createBulkPolicies(List.of(createPolicyDto)).results().getFirst();
    }

    private void validatePolicyAndThrowIfKeyNotFound(
            BulkPolicyModificationDtoWrapper<?> reqBulkDto) {
        var project = authorRequestScope.getProject();
        var reqPolicyKeys = reqBulkDto.policyKeySet.get();

        var dbPolicyList =
                policyRepository.findByKeyInAndProjectAndDeletedFalse(reqPolicyKeys, project);

        var dbPolicyKeys = dbPolicyList.stream().map(Policy::getKey).collect(Collectors.toSet());

        if (dbPolicyKeys.size() != reqPolicyKeys.size()) {
            reqPolicyKeys.removeAll(dbPolicyKeys);
            throw new PolicyNotFoundException(reqPolicyKeys);
        }
    }

    private void validatePolicyAlreadyExist(BulkPolicyModificationDtoWrapper<?> reqBulkDto) {

        var project = authorRequestScope.getProject();
        var reqPolicyKeys = reqBulkDto.policyKeySet.get();

        var dbPolicies =
                policyRepository.findByKeyInAndProjectAndDeletedFalse(reqPolicyKeys, project);

        if (!dbPolicies.isEmpty()) {

            var dbPolicyKeys =
                    dbPolicies.stream()
                            .map(Policy::getKey)
                            .filter(reqPolicyKeys::contains)
                            .toList();

            throw new PolicyAlreadyExistException(dbPolicyKeys);
        }
    }

    private ResourceActionMap findAndValidateResourceAction(
            BulkPolicyModificationDtoWrapper<?> reqBulkDto) {

        var project = authorRequestScope.getProject();

        // find resource-actions
        var reqResourceTypeKeySet = reqBulkDto.resourceTypeKeySet.get();
        var dbResourceActionList =
                resourceActionRepository.findAllByResourceTypeKeyIn(reqResourceTypeKeySet, project);

        var dbResourceActionMap = new ResourceActionMap(dbResourceActionList);

        // validate ResourceType
        if (reqResourceTypeKeySet.size() != dbResourceActionMap.countResourceTypeKey()) {
            var invalidResourceTypeKeys =
                    reqResourceTypeKeySet.stream()
                            .filter(k -> !dbResourceActionMap.containsResourceTypeKey(k))
                            .toList();
            throw new ResourceTypeNotFoundException(invalidResourceTypeKeys);
        }

        // validate action based on resource dataType
        for (var reqDto : reqBulkDto.getDtoList()) {
            var invalidActions =
                    reqDto.actions().stream()
                            .filter(
                                    action ->
                                            !dbResourceActionMap
                                                    .containsResourceTypeKeyAndActionName(
                                                            reqDto.resourceType(), action))
                            .toList();

            if (!invalidActions.isEmpty()) {
                throw new ActionNotFoundException(invalidActions);
            }
        }

        return dbResourceActionMap;
    }

    public ListResponseDto<PolicyDto> createBulkPolicies(
            List<CreatePolicyDto> reqCreatePolicyDtoList) {
        var reqBulkDto = new BulkPolicyModificationDtoWrapper<>(reqCreatePolicyDtoList);
        return createBulkPolicies(reqBulkDto);
    }

    @Autowired
    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    private class ScopeDbCache extends Cache<Map<String, Scope>> {

        public ScopeDbCache(BulkPolicyModificationDtoWrapper<?> reqBulkDto) {
            super(
                    () -> {
                        var reqScopeKeySet = reqBulkDto.scopeKeySetExcludingBlanks.get();
                        if (reqScopeKeySet.isEmpty()) {
                            return Map.of();
                        } else {
                            List<Scope> dbScopeList =
                                    scopeService.findByKeysAndThrowIfNotExists(reqScopeKeySet);
                            return dbScopeList.stream()
                                    .collect(Collectors.toMap(Scope::getKey, Function.identity()));
                        }
                    });
        }

        public Scope getBy(IPolicyModificationDto dto) {
            return getByScopeKey(dto.scope());
        }

        public Scope getByScopeKey(String scopeKey) {
            if (StringUtils.isBlank(scopeKey)) {
                return authorRequestScope.getDefaultScopeRbac().get();
            }
            return get().get(scopeKey);
        }
    }

    private ListResponseDto<PolicyDto> createBulkPolicies(
            BulkPolicyModificationDtoWrapper<CreatePolicyDto> reqBulkDto) {

        var project = authorRequestScope.getProject();
        var projectKey = project.getKey();

        validatePolicyAlreadyExist(reqBulkDto);

        // load, validate scope data from DB, and cache by scopeKey
        var dbScopeCache = new ScopeDbCache(reqBulkDto);

        // load, validate roles data from DB, and cache by roleKey
        var dbRoleMapByKey =
                roleService.findRolesAndThrowIfNotExists(reqBulkDto.roleKeySet.get()).stream()
                        .collect(Collectors.toMap(Role::getKey, Function.identity()));

        // load, validate resource action data from DB, and cache by resource dataType key and
        // action
        // name
        var dbResourceActionMap = findAndValidateResourceAction(reqBulkDto);

        // Validate Duplicate PolicyItems
        validateDuplicatePolicyItems(reqBulkDto);

        // prepare policy entity list with project and save policies data
        var newPolicyList =
                reqBulkDto.getDtoList().stream()
                        .map(policyMapper::toPolicy)
                        .peek(policy -> policy.setProject(project))
                        .toList();
        newPolicyList = policyRepository.saveAll(newPolicyList);
        auditService.commitAsync(newPolicyList);

        var policyMap =
                newPolicyList.stream()
                        .collect(Collectors.toMap(Policy::getKey, Function.identity()));

        List<PolicyItem> newPolicyItemList = new ArrayList<>();
        List<PolicyDto> respPolicyDtoList = new ArrayList<>();

        for (var reqPolicyCreate : reqBulkDto.getDtoList()) {

            Policy policy = policyMap.get(reqPolicyCreate.key());

            var scope = dbScopeCache.getBy(reqPolicyCreate);

            var reqResourceTypeKey = reqPolicyCreate.resourceType();
            for (String reqRoleKey : reqPolicyCreate.roles()) {
                Role role = dbRoleMapByKey.get(reqRoleKey);

                for (String reqActionName : reqPolicyCreate.actions()) {
                    var resourceAction =
                            dbResourceActionMap.getByResourceTypeKeyAndActionName(
                                    reqResourceTypeKey, reqActionName);

                    String rawKey =
                            String.join(
                                    "|",
                                    scope.getKey(),
                                    role.getKey(),
                                    resourceAction.getActionName(),
                                    resourceAction.getResourceType().getKey());

                    String policyItemKey = Utils.sha256(rawKey);

                    var policyItem =
                            PolicyItem.builder()
                                    .projectKey(projectKey)
                                    .policyItemKey(policyItemKey)
                                    .scope(scope)
                                    .role(role)
                                    .effect(reqPolicyCreate.effect())
                                    .resourceAction(resourceAction)
                                    .policy(policy)
                                    .build();

                    newPolicyItemList.add(policyItem);
                }
            }

            // TODO: should be from database instead of DTO
            var respPolicyDto = policyMapper.toPolicyDto(reqPolicyCreate, scope.getKey());
            respPolicyDtoList.add(respPolicyDto);
        }

        policyItemRepository.saveAll(newPolicyItemList);
        auditService.commitAsync(newPolicyItemList);

        return ListResponseDto.create(respPolicyDtoList);
    }

    public PolicyDto updatePolicy(String policyKey, UpdatePolicyDto updatePolicyDto) {
        var bulkDto = policyMapper.toUpdateBulkPolicyDto(policyKey, updatePolicyDto);
        var listRespDto = updateBulkPolicies(List.of(bulkDto));
        return listRespDto.results().getFirst();
    }

    public ListResponseDto<PolicyDto> updateBulkPolicies(
            List<UpdateBulkPolicyDto> reqPolicyUpdateDtoList) {

        var reqDto = new BulkPolicyModificationDtoWrapper<>(reqPolicyUpdateDtoList);

        return updateBulkPolicies(reqDto);
    }

    private static class PolicyDbCache {

        private final Map<PolicyItemUniqueKey, PolicyItem> dbPolicyItemKeyMap = new HashMap<>();
        private final Map<String, Policy> dbPolicyMapByKey = new HashMap<>();

        public PolicyDbCache(List<PolicyItem> dbPolicyItems) {
            for (var dbPolicyItem : dbPolicyItems) {
                var dbPolicyItemKey = PolicyItemUniqueKey.from(dbPolicyItem);
                dbPolicyItemKeyMap.put(dbPolicyItemKey, dbPolicyItem);
                dbPolicyMapByKey.put(dbPolicyItem.getPolicy().getKey(), dbPolicyItem.getPolicy());
            }
        }

        public PolicyItem getPolicyItem(PolicyItemUniqueKey reqPolicyItemKey) {
            return dbPolicyItemKeyMap.get(reqPolicyItemKey);
        }

        public Policy getPolicy(String policyKey) {
            return dbPolicyMapByKey.get(policyKey);
        }

        public Set<Map.Entry<PolicyItemUniqueKey, PolicyItem>> getPolicyItemEntrySet() {
            return dbPolicyItemKeyMap.entrySet();
        }
    }

    private ListResponseDto<PolicyDto> updateBulkPolicies(
            BulkPolicyModificationDtoWrapper<UpdateBulkPolicyDto> reqBulkDto) {
        var project = authorRequestScope.getProject();
        var projectKey = project.getKey();

        // throw if policy key not found
        validatePolicyAndThrowIfKeyNotFound(reqBulkDto);

        // load, validate and cache scope data from DB
        var dbScopeCache = new ScopeDbCache(reqBulkDto);

        var dbRoleMapByKey =
                roleService.findRolesAndThrowIfNotExists(reqBulkDto.roleKeySet.get()).stream()
                        .collect(Collectors.toMap(Role::getKey, Function.identity()));

        // create Map of resourceAction by resourceTypeKey
        var dbResourceActionMap = findAndValidateResourceAction(reqBulkDto);

        // throw if duplicate with other policy (diff key)
        validateDuplicatePolicyItems(reqBulkDto);

        var dbPolicyItems =
                policyItemRepository.findFetchAllByPolicyKeys(
                        reqBulkDto.policyKeySet.get(), authorRequestScope.getProject());
        PolicyDbCache dbPolicyMap = new PolicyDbCache(dbPolicyItems);

        // from Incoming request -> create Map Indexing of PolicyItemKey
        var reqPolicyItemKeyMap = reqBulkDto.policyItemKeyMap.get();

        var policyItemsToBeSaved = new ArrayList<PolicyItem>();

        reqPolicyItemKeyMap.forEach(
                (reqPolicyItemKey, reqPolicyUpdDto) -> {
                    PolicyItem existing = dbPolicyMap.getPolicyItem(reqPolicyItemKey);

                    // if not exist in current database -> PolicyItem to be created
                    if (existing == null) {
                        log.debug(
                                "policy item to be created: {}, effect: {}",
                                reqPolicyItemKey,
                                reqPolicyUpdDto.effect());

                        var newPolicyItem =
                                PolicyItem.builder()
                                        .projectKey(projectKey)
                                        .policyItemKey(reqPolicyItemKey.getPolicyItemKey())
                                        .scope(dbScopeCache.getBy(reqPolicyUpdDto))
                                        .role(dbRoleMapByKey.get(reqPolicyItemKey.roleKey))
                                        .policy(dbPolicyMap.getPolicy(reqPolicyUpdDto.key()))
                                        .resourceAction(
                                                dbResourceActionMap.getByPolicyItemKey(
                                                        reqPolicyItemKey))
                                        .effect(reqPolicyUpdDto.effect())
                                        .build();
                        policyItemsToBeSaved.add(newPolicyItem);
                    }
                    // if different effect -> to be updated
                    else if (existing.getEffect() != reqPolicyUpdDto.effect()) {
                        log.debug(
                                "policy item to be updated: {}, effect: {}, id: {}",
                                reqPolicyItemKey,
                                reqPolicyUpdDto.effect(),
                                existing.getId());

                        existing.setEffect(reqPolicyUpdDto.effect());
                        policyItemsToBeSaved.add(existing);
                    } else {
                        log.debug(
                                "policy item not changed: {}, effect: {}, id: {}",
                                reqPolicyItemKey,
                                reqPolicyUpdDto.effect(),
                                existing.getId());
                    }
                });
        if (!policyItemsToBeSaved.isEmpty()) {
            policyItemRepository.saveAll(policyItemsToBeSaved);
            auditService.commitAsync(policyItemsToBeSaved);
        }

        // find from existing data but not exist in the incoming request -> to be deleted
        var policyItemsToBeDeleted =
                dbPolicyMap.getPolicyItemEntrySet().stream()
                        .filter(entry -> !reqPolicyItemKeyMap.containsKey(entry.getKey()))
                        .peek(
                                entry ->
                                        log.debug(
                                                "policy item to be deleted: {}, effect: {}, id: {}",
                                                entry.getKey(),
                                                entry.getValue().getEffect(),
                                                entry.getValue().getId()))
                        .map(Map.Entry::getValue)
                        .toList();
        if (!policyItemsToBeDeleted.isEmpty()) {
            policyItemRepository.deleteAll(policyItemsToBeDeleted);
            auditService.commitAsync(policyItemsToBeDeleted);
        }

        var policyToBeUpdated = new ArrayList<Policy>();
        var respPolicyDtoList = new ArrayList<PolicyDto>();
        for (var reqDto : reqBulkDto.getDtoList()) {

            // update description for the policy entities
            var dbPolicy = dbPolicyMap.getPolicy(reqDto.key());
            if (!StringUtils.equals(dbPolicy.getDescription(), reqDto.description())) {
                dbPolicy.setDescription(reqDto.description());
                policyToBeUpdated.add(dbPolicy);
            }

            var scope = dbScopeCache.getBy(reqDto);
            // generate the response result
            // TODO: should be replace from Database instead from DTO
            var respPolicyDto = policyMapper.toPolicyDto(reqDto, scope.getKey());
            respPolicyDtoList.add(respPolicyDto);
        }
        if (!policyToBeUpdated.isEmpty()) {
            policyRepository.saveAll(policyToBeUpdated);
            auditService.commitAsync(policyToBeUpdated);
        }

        return ListResponseDto.create(respPolicyDtoList);
    }

    private static String escapeLike(String input) {
        if (input == null) return null;
        return input.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    // this class is for indexing the ResourceAction by Resource Type Key and Action Name
    private static class ResourceActionMap {

        // Map<String::ResourceTypeKey, Map<String::actionName, ResourceAction>>
        private final Map<String, Map<String, ResourceAction>> resourceActionMapByTypeKey =
                new HashMap<>();

        public ResourceActionMap(List<ResourceAction> resourceActions) {
            resourceActions.forEach(this::put);
        }

        private void put(ResourceAction resourceAction) {
            var resourceTypeKey = resourceAction.getResourceType().getKey();
            var actionNameMap =
                    resourceActionMapByTypeKey.computeIfAbsent(
                            resourceTypeKey, k -> new HashMap<>());
            actionNameMap.put(resourceAction.getActionName(), resourceAction);
        }

        public ResourceAction getByPolicyItemKey(PolicyItemUniqueKey policyItemKey) {
            return getByResourceTypeKeyAndActionName(
                    policyItemKey.resourceTypeKey(), policyItemKey.actionName());
        }

        public ResourceAction getByResourceTypeKeyAndActionName(
                String resourceTypeKey, String actionName) {
            var actionNameMap = resourceActionMapByTypeKey.get(resourceTypeKey);
            if (actionNameMap == null) {
                return null;
            }
            return actionNameMap.get(actionName);
        }

        public int countResourceTypeKey() {
            return resourceActionMapByTypeKey.size();
        }

        public boolean containsResourceTypeKey(String resourceTypeKey) {
            return resourceActionMapByTypeKey.containsKey(resourceTypeKey);
        }

        public boolean containsResourceTypeKeyAndActionName(
                String resourceTypeKey, String actionName) {
            var actionNameMap = resourceActionMapByTypeKey.get(resourceTypeKey);
            if (actionNameMap == null) {
                return false;
            }
            return actionNameMap.containsKey(actionName);
        }
    }

    @Builder
    private record PolicyItemUniqueKey(
            String resourceTypeKey, String scopeKey, String roleKey, String actionName) {
        static PolicyItemUniqueKey from(PolicyItem pi) {
            return new PolicyItemUniqueKey(
                    pi.getResourceAction().getResourceType().getKey(),
                    pi.getScope().getKey(),
                    pi.getRole().getKey(),
                    pi.getResourceAction().getActionName());
        }

        String getPolicyItemKey() {
            String rawKey = String.join("|", scopeKey, roleKey, actionName, resourceTypeKey);

            return Utils.sha256(rawKey);
        }
    }

    public void deletePolicy(String policyKey) {
        deletePolicy(List.of(policyKey));
    }

    public void deleteBulkPolicies(DeleteBulkPolicyDto deleteBulkPolicyDto) {

        var policyKeys = deleteBulkPolicyDto.keys();

        deletePolicy(policyKeys);
    }

    @Override
    public void deletePolicyItems(List<PolicyItem> policyItems) {

        Project project = authorRequestScope.getProject();

        policyItems.forEach(policyItem -> policyItem.setDeleted(true));
        policyItemRepository.saveAll(policyItems);
        auditService.commitAsync(policyItems);

        var policyKeysForQuery =
                policyItems.stream()
                        .map(PolicyItem::getPolicy)
                        .map(Policy::getKey)
                        .collect(Collectors.toSet());

        var policyListForDelete =
                policyRepository.findAllByKeyInAndHasNoItems(policyKeysForQuery, project);

        if (!policyListForDelete.isEmpty()) {
            String deletedDatetime =
                    ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));
            auditService.commitAsync(policyListForDelete);
            policyListForDelete.forEach(
                    policy -> {
                        policy.setKey(DELETED + "-" + deletedDatetime + "-" + policy.getKey());
                        policy.setDeleted(true);
                        log.debug("Policy deleted: {}", policy.getKey());
                    });
            policyRepository.saveAll(policyListForDelete);
        }
    }

    private void deletePolicy(List<String> policyKeys) {

        Project projectData = authorRequestScope.getProject();

        var dbPolicyMapByKey =
                policyRepository
                        .findByKeyInAndProjectAndDeletedFalse(policyKeys, projectData)
                        .stream()
                        .collect(Collectors.toMap(Policy::getKey, policy -> policy));

        var policyKeyListNotFound =
                policyKeys.stream()
                        .filter(policyKey -> !dbPolicyMapByKey.containsKey(policyKey))
                        .toList();
        if (!policyKeyListNotFound.isEmpty()) {
            throw new PolicyNotFoundException(policyKeyListNotFound);
        }

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        auditService.commitAsync(dbPolicyMapByKey.values());

        dbPolicyMapByKey.forEach(
                (policyKey, policy) -> {
                    policy.setKey(DELETED + "-" + deletedDatetime + "-" + policy.getKey());
                    policy.setDeleted(true);
                });

        policyRepository.saveAll(dbPolicyMapByKey.values());

        var policyItemList = policyItemRepository.findAllByPolicyKeyIn(dbPolicyMapByKey.keySet());
        policyItemList.forEach(
                policyItem -> {
                    policyItem.setDeleted(true);
                });
        policyItemRepository.saveAll(policyItemList);

        log.info(
                "Successfully Deleted Policies: {} from Project: {}",
                policyKeys,
                projectData.getKey());
    }

    public PolicyDto getPolicyByKey(String policyKey) {

        Project project = authorRequestScope.getProject();

        var exists = policyRepository.existsByKeyAndProjectAndDeletedFalse(policyKey, project);
        if (!exists) {
            throw new PolicyNotFoundException(policyKey);
        }

        var policyItemEntities = policyItemRepository.findWithFetchAllByPolicyKey(policyKey);

        return toPolicyDto(policyKey, policyItemEntities);
    }

    private PolicyDto toPolicyDto(String policyKey, List<PolicyItem> policyItems) {

        var policy =
                policyItems.stream()
                        .map(PolicyItem::getPolicy)
                        .filter(p -> p.getKey().equals(policyKey))
                        .findFirst()
                        .get();

        var resourceType = policyItems.getFirst().getResourceAction().getResourceType();

        var roles =
                policyItems.stream().map(PolicyItem::getRole).map(Role::getKey).distinct().toList();

        var actions =
                policyItems.stream()
                        .map(PolicyItem::getResourceAction)
                        .map(ResourceAction::getActionName)
                        .distinct()
                        .toList();

        var effect =
                policyItems.stream().map(PolicyItem::getEffect).map(Effect::name).findFirst().get();

        var scopeKeys =
                policyItems.stream()
                        .map(PolicyItem::getScope)
                        .filter(Objects::nonNull)
                        .map(Scope::getKey)
                        .findFirst()
                        .get();

        return PolicyDto.builder()
                .key(policy.getKey())
                .description(policy.getDescription())
                .scopeKey(scopeKeys)
                .roles(roles)
                .actions(actions)
                .resourceType(resourceType.getKey())
                .effect(effect)
                .build();
    }

    @RequiredArgsConstructor
    private class BulkPolicyModificationDtoWrapper<T extends IPolicyModificationDto> {

        @Getter private final List<T> dtoList;

        private String getScopeKeyOrDefault(T dto) {
            return Optional.ofNullable(dto.scope())
                    .orElseGet(() -> authorRequestScope.getDefaultScopeRbac().get().getKey());
        }

        public final Cache<Set<String>> policyKeySet =
                new Cache<>(() -> getDtoList().stream().map(T::key).collect(Collectors.toSet()));

        public final Cache<Map<PolicyItemUniqueKey, T>> policyItemKeyMap =
                new Cache<>(
                        () -> {
                            var reqPolicyItemKeyMap = new HashMap<PolicyItemUniqueKey, T>();
                            for (var dto : getDtoList()) {
                                for (String reqRoleKey : dto.roles()) {
                                    for (String reqActionName : dto.actions()) {
                                        var reqPolicyItemKey =
                                                PolicyItemUniqueKey.builder()
                                                        .resourceTypeKey(dto.resourceType())
                                                        .scopeKey(getScopeKeyOrDefault(dto))
                                                        .roleKey(reqRoleKey)
                                                        .actionName(reqActionName)
                                                        .build();
                                        reqPolicyItemKeyMap.put(reqPolicyItemKey, dto);
                                    }
                                }
                            }
                            return reqPolicyItemKeyMap;
                        });

        public final Cache<Set<String>> roleKeySet =
                new Cache<>(
                        () ->
                                getDtoList().stream()
                                        .flatMap(dto -> dto.roles().stream())
                                        .collect(Collectors.toSet()));

        public final Cache<Set<String>> scopeKeySetOrDefaults =
                new Cache<>(
                        () ->
                                getDtoList().stream()
                                        .map(this::getScopeKeyOrDefault)
                                        .collect(Collectors.toSet()));

        public final Cache<Set<String>> scopeKeySetExcludingBlanks =
                new Cache<>(
                        () ->
                                getDtoList().stream()
                                        .map(T::scope)
                                        .filter(StringUtils::isNotBlank)
                                        .collect(Collectors.toSet()));

        public final Cache<Set<String>> actionNameSet =
                new Cache<>(
                        () ->
                                getDtoList().stream()
                                        .flatMap(dto -> dto.actions().stream())
                                        .collect(Collectors.toSet()));

        public final Cache<Set<String>> resourceTypeKeySet =
                new Cache<>(
                        () ->
                                getDtoList().stream()
                                        .map(T::resourceType)
                                        .collect(Collectors.toSet()));
    }

    private void validateDuplicatePolicyItems(BulkPolicyModificationDtoWrapper<?> reqDto) {

        var project = authorRequestScope.getProject();
        var reqPolicyKeySet = reqDto.policyKeySet.get();

        var dbPolicyItems =
                policyItemRepository
                        .findAllByResourceTypeKeyInAndActionNameInAndRoleKeyInAndScopeKeyIn(
                                reqDto.resourceTypeKeySet.get(),
                                reqDto.actionNameSet.get(),
                                reqDto.roleKeySet.get(),
                                reqDto.scopeKeySetOrDefaults.get(),
                                project);

        // dbPolicyItemDuplicateList <- find all policyItems from other Policy (different
        // policyKey)
        var dbPolicyItemDuplicateList =
                dbPolicyItems.stream()
                        .filter(pi -> !reqPolicyKeySet.contains(pi.getPolicy().getKey()))
                        .toList();
        if (!dbPolicyItemDuplicateList.isEmpty()) {
            throw new DuplicatePolicyItemException(dbPolicyItemDuplicateList);
        }
    }
}
