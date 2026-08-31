// this class is partially vibe coded, beware

package com.hmg.role.abac.permission;

import static com.hmg.role.abac.permission.dto.AbacPermissionRequestDto.AbacInstanceValuesDto;
import static com.hmg.role.abac.permission.dto.AbacPermissionRequestDto.AbacResourceActionsDto;
import static com.hmg.role.abac.permission.dto.AbacPermissionResponseDto.ActionEffectDto;
import static com.hmg.role.sdk.common.util.Utils.isAllEqual;

import com.hmg.role.abac.common.exceptions.AbacAttributeInvalidTypeException;
import com.hmg.role.abac.common.exceptions.AbacNullPermissionAttributeValue;
import com.hmg.role.abac.logicalexpression.ConditionEvaluationService;
import com.hmg.role.abac.logicalexpression.ConditionalExpression;
import com.hmg.role.abac.logicalexpression.LogicalExpressionMapper;
import com.hmg.role.abac.permission.dto.AbacPermissionFlattenedResponseDto;
import com.hmg.role.abac.permission.dto.AbacPermissionRequestDto;
import com.hmg.role.abac.permission.dto.AbacPermissionResponseDto;
import com.hmg.role.abac.permission.interfaces.AbacPermissionService;
import com.hmg.role.abac.policy.AbacPolicyItem;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemRepository;
import com.hmg.role.abac.resourceset.ResourceSet;
import com.hmg.role.abac.resourceset.ResourceSetRepository;
import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.userset.UserSet;
import com.hmg.role.abac.userset.UserSetRepository;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.rbac.scope.interfaces.ScopeService;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.entity.AbstractConditionOperand;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AbacPermissionServiceImpl implements AbacPermissionService {
    private static final int DEPTH_COUNTER = 1;
    private static final int MAX_DEPTH_COUNTER = 2;

    private final UserSetRepository userSetRepository;
    private final ResourceSetRepository resourceSetRepository;
    private final AbacPolicyItemRepository abacPolicyItemRepository;
    private final AbacPermissionMapper abacPermissionMapper;
    private final ConditionEvaluationService conditionEvaluationService;
    private final LogicalExpressionMapper logicalExpressionMapper;
    private final ScopeService scopeService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    public AbacPermissionServiceImpl(
            // necessary to distinguish between the impl beans
            // required because the business logic is ridiculous
            // TODO refactor these when we have a time
            UserSetRepository userSetRepository,
            ResourceSetRepository resourceSetRepository,
            AbacPolicyItemRepository abacPolicyItemRepository,
            AbacPermissionMapper abacPermissionMapper,
            ConditionEvaluationService conditionEvaluationService,
            LogicalExpressionMapper logicalExpressionMapper,
            @Qualifier("abacScopeServiceImpl") ScopeService scopeService) {
        this.userSetRepository = userSetRepository;
        this.resourceSetRepository = resourceSetRepository;
        this.abacPolicyItemRepository = abacPolicyItemRepository;
        this.abacPermissionMapper = abacPermissionMapper;
        this.conditionEvaluationService = conditionEvaluationService;
        this.logicalExpressionMapper = logicalExpressionMapper;
        this.scopeService = scopeService;
    }

    @Override
    public ListResponseDto<AbacPermissionResponseDto> getAllPermissions(
            AbacPermissionRequestDto request) {
        return ListResponseDto.create(streamPermissionChecking(request).toList());
    }

    @Override
    public ListResponseDto<AbacPermissionFlattenedResponseDto> getAllPermissionsFlattened(
            AbacPermissionRequestDto request) {
        var permissions =
                streamPermissionChecking(request)
                        .flatMap(abacPermissionMapper::toPermissionFlattenedResponseDtoStream)
                        .toList();
        return ListResponseDto.create(permissions);
    }

    private Stream<AbacPermissionResponseDto> streamPermissionChecking(
            AbacPermissionRequestDto request) {
        return request.resources().stream()
                .map(res -> new SingleResourcePermissionRequestDto(request.user(), res))
                .map(this::getSingleResourcePermission)
                .flatMap(List::stream);
    }

    /**
     * This function is designed to find user sets, resource sets, and policy items based on
     * attributes requested from the payload. If the policy items are not available for the
     * requested attributes, all effects should return DENY. Otherwise, the results should be based
     * on the policy database. If any policy item effect is ALLOW, then the result should ALLOW. The
     * final step is to check the prioritized permission result.
     *
     * @return The List of final permission results.
     */
    private List<AbacPermissionResponseDto> getSingleResourcePermission(
            SingleResourcePermissionRequestDto request) {

        // 1. Reject null attribute values early before any evaluation
        validateAttributes(request.user().attributes(), "user");
        validateAttributes(request.resourceActions().resource().attributes(), "resource");

        var userScope = request.user().scope();
        var resourceScope = request.resourceActions().resource().scope();

        // [SCOPE_CHECK] User scope and resource scope must match
        if (!isAllEqual(userScope, resourceScope)) {
            log.info(
                    "[SCOPE_CHECK] Scope mismatch - userScope: {}, resourceScope: {}. Returning DENY.",
                    userScope,
                    resourceScope);
            return buildPermissionDeniedResponses(
                    request, new PolicySearchResults(List.of(), List.of(), List.of()));
        }

        // [SCOPE_CHECK] Scope must exist in the project
        if (!scopeService.existsScopeKey(userScope, getCurrentProject())) {
            log.info(
                    "[SCOPE_CHECK] Scope '{}' does not exist in project. Returning DENY.",
                    userScope);
            return buildPermissionDeniedResponses(
                    request, new PolicySearchResults(List.of(), List.of(), List.of()));
        }

        // 2. Find user/resource sets whose conditions evaluate true against the request attributes
        var matchedSets = lookupMatchingSets(request);
        // 3. Find policy items for those matched sets (initial DB query before hierarchy expansion)
        var policySearchResults = queryPolicyItems(request, matchedSets);

        // 4. Expand via hierarchy if needed, filter to attribute-evaluated sets, emit policy rows
        var permissionResponses = processAndGeneratePermissions(request, policySearchResults);

        // 5. No policy-backed rows survived → emit a single DENY per action as the fallback
        if (permissionResponses.isEmpty()) {
            permissionResponses = buildPermissionDeniedResponses(request, policySearchResults);
        }

        return permissionResponses;
    }

    /**
     * Retrieves policy items hierarchically by evaluated user sets, filters them to only those
     * whose user/resource sets passed attribute evaluation, and generates permissions. Steps: 1.
     * Retrieve policy items using a hierarchical search through user sets. 2. Discard items whose
     * user set or resource set was introduced by hierarchy traversal (not evaluated against the
     * request attributes). 3. Group the remaining policy items by resource set. 4. Emit one
     * response row per surviving policy item — only policy-backed rows are returned.
     *
     * @return The List of final permission results.
     */
    private List<AbacPermissionResponseDto> processAndGeneratePermissions(
            SingleResourcePermissionRequestDto request, PolicySearchResults policySearchResults) {

        var userSets = policySearchResults.userSets();
        var resourceSets = policySearchResults.resourceSets();
        var actions = request.resourceActions().actions();
        var currentPolicyItems = policySearchResults.policyItems();
        var scopeKey = request.user().scope();

        // Walk user/resource set hierarchy to find inherited policies when direct lookup is empty
        currentPolicyItems =
                getPolicyItems(currentPolicyItems, userSets, resourceSets, actions, scopeKey);

        // Discard policy items from hierarchy-traversed parent sets — only sets whose conditions
        // actually evaluated true against the request attributes may appear in the output
        var evaluatedUserSetKeys =
                userSets.stream().map(UserSet::getKey).collect(Collectors.toSet());
        var evaluatedResourceSetKeys =
                resourceSets.stream().map(ResourceSet::getKey).collect(Collectors.toSet());
        currentPolicyItems =
                currentPolicyItems.stream()
                        .filter(pi -> evaluatedUserSetKeys.contains(pi.getUserSet().getKey()))
                        .filter(
                                pi ->
                                        evaluatedResourceSetKeys.contains(
                                                pi.getResourceSetAction()
                                                        .getResourceSet()
                                                        .getKey()))
                        .toList();

        // Group by resource set so each response entry covers one matched resource
        var policyItemGroupByResourceSet = groupByResourceSet(currentPolicyItems);

        // Emit one response row per policy item; empty map → caller falls through to DENY
        return generatePermissionsBasedOnPolicyItems(request, policyItemGroupByResourceSet);
    }

    private List<AbacPolicyItem> getPolicyItems(
            List<AbacPolicyItem> currentPolicyItems,
            List<UserSet> userSets,
            List<ResourceSet> resourceSets,
            List<String> actions,
            String scopeKey) {

        // Perform user set inheritance
        if (currentPolicyItems.isEmpty()) {
            // if the policy items query is empty, then we need to getting all the User Set param
            // and re-querying it
            // by using parent User Sets
            var userSetKeys = userSets.stream().map(UserSet::getKey).distinct().toList();
            log.info(
                    "ABAC Policy Item is empty, trying to get a parent of User Set : {}",
                    userSetKeys);
            var userSetParents = getParentUserSets(userSets);
            if (!userSetParents.isEmpty()) {
                currentPolicyItems =
                        findPolicyItemsByUserSetHierarchy(
                                userSetParents, resourceSets, actions, DEPTH_COUNTER, scopeKey);
            } else {
                log.info("User Set has no parents, skipping user set hierarchy search");
            }
        } else {
            // Check the queried user set size data is same as user set in policy
            var policyUserSet =
                    currentPolicyItems.stream().map(AbacPolicyItem::getUserSet).distinct().toList();
            if (policyUserSet.size() != userSets.size()) {
                // if it is not same, then we need to re-querying it with updatedUserSets
                var updatedUserSets = updateUserSetsWithParents(userSets, policyUserSet);
                currentPolicyItems =
                        findPolicyItemsByUserSetHierarchy(
                                updatedUserSets, resourceSets, actions, DEPTH_COUNTER, scopeKey);
            }
        }

        /*If policy still not found based on previous user set inheritance check,
         We need to perform resource set inheritance and get policy items
        */
        if (currentPolicyItems.isEmpty()) {
            var resourceSetKeys = resourceSets.stream().map(ResourceSet::getKey).toList();
            log.info(
                    "ABAC Policy Item is empty, trying to get a parent of Resource Set : {}",
                    resourceSetKeys);
            var resourceSetParents = getParentResourceSets(resourceSets);
            if (!resourceSetParents.isEmpty()) {
                currentPolicyItems =
                        findPolicyItemsByResourceSetHierarchy(
                                userSets, resourceSetParents, actions, DEPTH_COUNTER, scopeKey);
            } else {
                log.info("Resource Set has no parents, skipping resource set hierarchy search");
            }
        } else {
            // Check the queried resource set size data is same as resource set in policy
            var policyResourceSet =
                    currentPolicyItems.stream()
                            .map(AbacPolicyItem::getResourceSetAction)
                            .map(ResourceSetAction::getResourceSet)
                            .distinct()
                            .toList();

            if (policyResourceSet.size() != resourceSets.size()) {
                // if it is not same, then we need to re-querying it with updatedResourceSets
                var updatedResourceSets =
                        updateResourceSetsWithParents(resourceSets, policyResourceSet);
                currentPolicyItems =
                        findPolicyItemsByResourceSetHierarchy(
                                userSets, updatedResourceSets, actions, DEPTH_COUNTER, scopeKey);
            }
        }

        return currentPolicyItems;
    }

    private List<AbacPolicyItem> findPolicyItemsByUserSetHierarchy(
            List<UserSet> userSets,
            List<ResourceSet> resourceSets,
            List<String> actions,
            int depth,
            String scopeKey) {

        Project project = getCurrentProject();

        var policyItems =
                abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        resourceSets, actions, userSets, project, scopeKey);

        var userSetKeys = userSets.stream().map(UserSet::getKey).distinct().toList();
        var resourceSetKeys = resourceSets.stream().map(ResourceSet::getKey).toList();

        if (policyItems.isEmpty() && depth < MAX_DEPTH_COUNTER) {
            // if the policy items are empty and not reached maximum depth, getting a parent of user
            // set
            log.info(
                    "ABAC Policy Item is empty, trying to get a parent of userSet : {}",
                    userSetKeys);

            var userSetParents = getParentUserSets(userSets);
            if (!userSetParents.isEmpty()) {
                return findPolicyItemsByUserSetHierarchy(
                        userSetParents, resourceSets, actions, depth + DEPTH_COUNTER, scopeKey);
            } else {
                log.info("No parent user sets found, ending hierarchy search");
                return List.of();
            }
        } else if (depth < MAX_DEPTH_COUNTER) {
            /* if the policy items are not empty and not reached maximum depth, check the
            query result of policy user set which is same as user set. if not same, find the parent
            of non-existing user set
             */
            var policyUserSets =
                    policyItems.stream().map(AbacPolicyItem::getUserSet).distinct().toList();

            if (policyUserSets.size() != userSets.size()) {
                var updatedUserSets = updateUserSetsWithParents(userSets, policyUserSets);
                return findPolicyItemsByUserSetHierarchy(
                        updatedUserSets, resourceSets, actions, depth + DEPTH_COUNTER, scopeKey);
            }
        }

        if (policyItems.isEmpty()) {
            // if the policy items is still empty and reached the maximum depth, print log message
            log.info(
                    "ABAC Policy Item not found with given User Sets: {} and Resource Sets: {}",
                    userSetKeys,
                    resourceSetKeys);
            return policyItems;
        }

        log.info(
                "ABAC Policy Item are found with given User Sets: {} and Resource Sets: {}",
                userSetKeys,
                resourceSetKeys);
        return policyItems;
    }

    private List<AbacPolicyItem> findPolicyItemsByResourceSetHierarchy(
            List<UserSet> userSets,
            List<ResourceSet> resourceSets,
            List<String> actions,
            int depth,
            String scopeKey) {

        Project project = getCurrentProject();

        var policyItems =
                abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        resourceSets, actions, userSets, project, scopeKey);

        var userSetKeys = userSets.stream().map(UserSet::getKey).distinct().toList();
        var resourceSetKeys = resourceSets.stream().map(ResourceSet::getKey).toList();

        if (policyItems.isEmpty() && depth < MAX_DEPTH_COUNTER) {
            // if the policy items are empty and not reached maximum depth, getting a parent of
            // resource set
            log.info(
                    "ABAC Policy Item is empty, trying to get a parent of resource set : {}",
                    resourceSetKeys);

            var resourceSetParents = getParentResourceSets(resourceSets);
            if (!resourceSetParents.isEmpty()) {
                return findPolicyItemsByResourceSetHierarchy(
                        userSets, resourceSetParents, actions, depth + DEPTH_COUNTER, scopeKey);
            } else {
                log.info("No parent resource sets found, ending hierarchy search");
                return List.of();
            }
        } else if (depth < MAX_DEPTH_COUNTER) {
            /* if the policy items are not empty and not reached maximum depth, check the
            query result of policy user set which is same as user set. if not same, find the parent
            of non-existing user set
             */
            var policyResourceSets =
                    policyItems.stream()
                            .map(AbacPolicyItem::getResourceSetAction)
                            .map(ResourceSetAction::getResourceSet)
                            .distinct()
                            .toList();

            if (policyResourceSets.size() != resourceSets.size()) {
                var updatedResourceSets =
                        updateResourceSetsWithParents(resourceSets, policyResourceSets);
                return findPolicyItemsByResourceSetHierarchy(
                        userSets, updatedResourceSets, actions, depth + DEPTH_COUNTER, scopeKey);
            }
        }

        if (policyItems.isEmpty()) {
            // if the policy items is still empty and reached the maximum depth, print log message
            log.info(
                    "ABAC Policy Item not found with given User Sets: {} and Resource Sets: {}",
                    userSetKeys,
                    resourceSetKeys);
            return policyItems;
        }

        log.info(
                "ABAC Policy Item are found with given User Sets: {} and Resource Sets: {}",
                userSetKeys,
                resourceSetKeys);
        return policyItems;
    }

    private List<UserSet> getParentUserSets(List<UserSet> userSets) {

        return userSets.stream()
                .flatMap(userSet -> userSet.getParents().stream())
                .distinct()
                .toList();
    }

    private List<ResourceSet> getParentResourceSets(List<ResourceSet> resourceSets) {

        return resourceSets.stream()
                .map(ResourceSet::getParent)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<UserSet> updateUserSetsWithParents(
            List<UserSet> currentUserSet, List<UserSet> policyUserSets) {

        var unknownUserSets =
                currentUserSet.stream()
                        .filter(userSet -> !policyUserSets.contains(userSet))
                        .toList();

        var unknownUserSetKeys = unknownUserSets.stream().map(UserSet::getKey).distinct().toList();

        log.info(
                "ABAC Policy Item with given user set is not same, trying to get a parent of user set : {}",
                unknownUserSetKeys);

        var userSetParents = getParentUserSets(unknownUserSets);

        currentUserSet = currentUserSet.stream().filter(policyUserSets::contains).toList();

        List<UserSet> updatedUserSets = new ArrayList<>(currentUserSet);
        updatedUserSets.addAll(userSetParents);

        return updatedUserSets;
    }

    private List<ResourceSet> updateResourceSetsWithParents(
            List<ResourceSet> currentResourceSets, List<ResourceSet> policyResourceSets) {

        var unknownResourceSets =
                currentResourceSets.stream()
                        .filter(resourceSet -> !policyResourceSets.contains(resourceSet))
                        .toList();

        var unknownResourceSetKeys =
                unknownResourceSets.stream().map(ResourceSet::getKey).distinct().toList();

        log.info(
                "ABAC Policy Item with given resource set is not same, trying to get a parent of resource set : {}",
                unknownResourceSetKeys);

        var resourceSetParents = getParentResourceSets(unknownResourceSets);

        currentResourceSets =
                currentResourceSets.stream().filter(policyResourceSets::contains).toList();

        List<ResourceSet> updatedResourceSets = new ArrayList<>(currentResourceSets);
        updatedResourceSets.addAll(resourceSetParents);

        return updatedResourceSets;
    }

    // Only emits rows backed by an actual policy item — no synthetic DENY rows for unpolicied sets
    private List<AbacPermissionResponseDto> generatePermissionsBasedOnPolicyItems(
            SingleResourcePermissionRequestDto request,
            Map<ResourceSet, List<AbacPolicyItem>> policyItemMap) {

        var permissionResponses = new ArrayList<AbacPermissionResponseDto>();
        for (var entry : policyItemMap.entrySet()) {
            var actionEffects =
                    entry.getValue().stream().map(abacPermissionMapper::toActionEffectDto).toList();

            var resourceSetKey = entry.getKey().getKey();

            var resource =
                    abacPermissionMapper.toResourceResponseDto(
                            request.resourceActions().resource(), resourceSetKey);

            permissionResponses.add(new AbacPermissionResponseDto(resource, actionEffects));
        }

        return permissionResponses;
    }

    // Fallback when no policy-backed rows exist; userSet is null because no policy owns the DENY
    private List<AbacPermissionResponseDto> buildPermissionDeniedResponses(
            SingleResourcePermissionRequestDto request, PolicySearchResults policySearchResults) {
        var resourceSets = policySearchResults.resourceSets();

        // resourceSet key is populated if a matching resource set was found in DB, otherwise null
        List<String> resourceSetKeys =
                resourceSets.isEmpty()
                        ? Collections.singletonList(null)
                        : resourceSets.stream().map(ResourceSet::getKey).toList();

        List<AbacPermissionResponseDto> permissionResponses = new ArrayList<>();
        for (var resourceSetKey : resourceSetKeys) {
            var resource =
                    abacPermissionMapper.toResourceResponseDto(
                            request.resourceActions().resource(), resourceSetKey);
            var actionEffects =
                    request.resourceActions().actions().stream()
                            .map(action -> new ActionEffectDto(null, action, Effect.DENY))
                            .toList();
            permissionResponses.add(new AbacPermissionResponseDto(resource, actionEffects));
        }
        return permissionResponses;
    }

    private Map<ResourceSet, List<AbacPolicyItem>> groupByResourceSet(
            List<AbacPolicyItem> policyItems) {

        Map<ResourceSet, List<AbacPolicyItem>> policyMap = new HashMap<>();

        for (AbacPolicyItem policyItem : policyItems) {
            List<AbacPolicyItem> policyItems1 =
                    policyMap.get(policyItem.getResourceSetAction().getResourceSet());

            if (policyItems1 == null) {
                policyItems1 = new ArrayList<>();
                policyMap.put(policyItem.getResourceSetAction().getResourceSet(), policyItems1);
            }

            policyItems1.add(policyItem);
        }
        return policyMap;
    }

    private static void validateAttributes(Map<String, Object> attributes, String subject) {
        if (attributes == null || attributes.containsValue(null)) {
            throw new AbacNullPermissionAttributeValue(subject);
        }
    }

    /**
     * Looks up and filters user sets and resource sets matching the request attributes.
     *
     * @return PolicySearchResults with empty policyItems and the filtered user/resource sets.
     */
    private PolicySearchResults lookupMatchingSets(SingleResourcePermissionRequestDto request) {
        Project project = getCurrentProject();

        //  Note: there is a possibility that attributes has a complex structure (i.e. nested object
        // or array)
        //  If there is some nested object or array, needs improvements on these codes
        var mapUserAttributes = request.user().attributes();
        var mapResourceAttributes = request.resourceActions().resource().attributes();

        var userSets =
                userSetRepository.findAllByAttributesAndProject(
                        mapUserAttributes.keySet(), project);
        var filteredUserSets = filterUserSets(userSets, mapUserAttributes);
        log.info(
                "Filtered user sets count: {}, keys: {}",
                filteredUserSets.size(),
                filteredUserSets.stream().map(UserSet::getKey).toList());

        var resourceSets =
                resourceSetRepository.findByAttributesAndProject(
                        mapResourceAttributes.keySet(), project);
        var filteredResourceSets = filterResourceSets(resourceSets, mapResourceAttributes);

        return new PolicySearchResults(List.of(), filteredUserSets, filteredResourceSets);
    }

    /**
     * Queries policy items using the already-resolved user sets and resource sets. Only called
     * after scope validation passes.
     */
    private PolicySearchResults queryPolicyItems(
            SingleResourcePermissionRequestDto request, PolicySearchResults matchedSets) {
        Project project = getCurrentProject();
        var filteredUserSets = matchedSets.userSets();
        var filteredResourceSets = matchedSets.resourceSets();
        var userScope = request.user().scope();

        log.info(
                "Querying policy items with: userSets={}, resourceSets={}, actions={}, scope={}",
                filteredUserSets.stream().map(UserSet::getKey).toList(),
                filteredResourceSets.stream().map(ResourceSet::getKey).toList(),
                request.resourceActions().actions(),
                userScope);

        var policyItems =
                abacPolicyItemRepository.findAllByResourceActionsAndUserSetAndProject(
                        filteredResourceSets,
                        request.resourceActions().actions(),
                        filteredUserSets,
                        project,
                        userScope);

        log.info("Found {} policy items from initial query", policyItems.size());

        return new PolicySearchResults(policyItems, filteredUserSets, filteredResourceSets);
    }

    private Project getCurrentProject() {
        Project project = authorRequestScope.getProject();
        if (project == null) {
            project = authorRequestScope.getMember().getProject();
        }
        return project;
    }

    private List<UserSet> filterUserSets(
            List<UserSet> userSets, Map<String, Object> userAttributes) {
        var filteredUserSets = new ArrayList<UserSet>();
        for (var userSet : userSets) {
            var evalResult = evaluate(userSet, userAttributes);
            if (evalResult) {
                filteredUserSets.add(userSet);
            }
        }
        return filteredUserSets;
    }

    private boolean evaluate(UserSet userSet, Map<String, Object> userAttributes) {
        for (var condition : userSet.getConditions()) {
            validateAttributeType(condition.getLeftOperand(), userAttributes);
            validateAttributeType(condition.getRightOperand(), userAttributes);
        }
        List<ConditionalExpression> conditionalExpressions =
                userSet.getConditions().stream()
                        .map(
                                condition ->
                                        logicalExpressionMapper.toConditionalExpression(
                                                condition.getOperator(),
                                                condition.getLeftOperand(),
                                                condition.getRightOperand()))
                        .toList();
        var logicalOperator =
                LogicalExpressionMapper.toLogicalOperator(userSet.getConditionGroupOperator());
        var res =
                conditionEvaluationService.evaluateConditionalGroup(
                        logicalOperator, conditionalExpressions, userAttributes);
        return res.output();
    }

    private List<ResourceSet> filterResourceSets(
            List<ResourceSet> resourceSets, Map<String, Object> resourceAttributes) {
        var filteredResourceSets = new ArrayList<ResourceSet>();
        for (var resourceSet : resourceSets) {
            for (var condition : resourceSet.getConditionGroup()) {
                validateAttributeType(condition.getLeftOperand(), resourceAttributes);
                validateAttributeType(condition.getRightOperand(), resourceAttributes);
            }
            List<ConditionalExpression> conditionalExpressions =
                    resourceSet.getConditionGroup().stream()
                            .map(
                                    condition ->
                                            logicalExpressionMapper.toConditionalExpression(
                                                    condition.getOperator(),
                                                    condition.getLeftOperand(),
                                                    condition.getRightOperand()))
                            .toList();
            var logicalOperator =
                    LogicalExpressionMapper.toLogicalOperator(
                            resourceSet.getConditionGroupOperator());
            var res =
                    conditionEvaluationService.evaluateConditionalGroup(
                            logicalOperator, conditionalExpressions, resourceAttributes);
            var evalResult = res.output();
            if (evalResult) {
                filteredResourceSets.add(resourceSet);
            }
        }
        return filteredResourceSets;
    }

    private static void validateAttributeType(
            AbstractConditionOperand operand, Map<String, Object> attributes) {
        if (operand.getType() != OperandType.ATTRIBUTE) return;
        var dataType = operand.getDataType();
        if (dataType == OperandDataType.STRING) return;
        var value = attributes.get(operand.getOperand());
        if (value == null) return;
        var valid =
                switch (dataType) {
                    case NUMBER -> value instanceof Number;
                    case BOOLEAN -> value instanceof Boolean;
                    case STRING -> true;
                };
        if (!valid) {
            throw new AbacAttributeInvalidTypeException(operand.getOperand(), dataType);
        }
    }

    private record SingleResourcePermissionRequestDto(
            AbacInstanceValuesDto user, AbacResourceActionsDto resourceActions) {}

    private record PolicySearchResults(
            List<AbacPolicyItem> policyItems,
            List<UserSet> userSets,
            List<ResourceSet> resourceSets) {}
}
