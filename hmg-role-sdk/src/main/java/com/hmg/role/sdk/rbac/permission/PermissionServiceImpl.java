package com.hmg.role.sdk.rbac.permission;

import static com.hmg.role.sdk.rbac.permission.dto.PermissionResponseDto.ActionEffectDto;

import com.hmg.role.sdk.common.enums.Effect;
import com.hmg.role.sdk.common.exception.NotFoundException;
import com.hmg.role.sdk.common.util.StringUtils;
import com.hmg.role.sdk.rbac.permission.dto.*;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByRole;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByRoleDto;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByUser;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponse;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponseDto;
import com.hmg.role.sdk.rbac.permission.dto.PermissionResponse;
import com.hmg.role.sdk.rbac.permission.dto.PermissionResponseDto;
import com.hmg.role.sdk.rbac.permission.dto.ResourceRequest;
import com.hmg.role.sdk.rbac.permission.dto.RoleSubjectRequest;
import com.hmg.role.sdk.rbac.permission.dto.UserSubjectRequest;
import com.hmg.role.sdk.rbac.permission.exception.*;
import com.hmg.role.sdk.rbac.permission.exception.PolicyNotFoundException;
import com.hmg.role.sdk.rbac.permission.exception.RoleByUserAndScopeNotFoundException;
import com.hmg.role.sdk.rbac.permission.model.*;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemKey;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemKeyDto;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionSetModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.RoleSetModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.model.UserModel;
import com.hmg.role.sdk.rbac.permission.spi.*;
import com.hmg.role.sdk.rbac.permission.spi.PolicyItemProvider;
import com.hmg.role.sdk.rbac.permission.spi.ResourceActionProvider;
import com.hmg.role.sdk.rbac.permission.spi.RoleProvider;
import com.hmg.role.sdk.rbac.permission.spi.ScopeProvider;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionServiceImpl implements PermissionService {

    private final PolicyItemProvider policyItemProvider;
    private final RoleProvider roleProvider;
    private final ScopeProvider scopeProvider;
    private final ResourceActionProvider resourceActionProvider;

    private final DataExistenceValidator dataExistenceValidator;

    @Builder.Default private PermissionMapper permissionMapper = PermissionMapper.INSTANCE;

    @Builder.Default
    private DataNotFoundStrategy dataNotFoundStrategy = DataNotFoundStrategy.RETURN_PERMISSION_DENY;

    @Override
    public Collection<? extends PermissionResponse> getPermissions(
            UserSubjectRequest userSubject, Collection<? extends ResourceRequest> resources)
            throws NotFoundException {

        RoleSetModel roleSet = getRolesByUserAndScope(userSubject, userSubject);

        return getPermissions(roleSet, resources);
    }

    @Override
    public Collection<? extends PermissionResponse> getPermissions(
            RoleSubjectRequest roleSubject, Collection<? extends ResourceRequest> resources)
            throws NotFoundException {

        return getPermissions((RoleSetModel) roleSubject, resources);
    }

    private Collection<? extends PermissionResponse> getPermissions(
            RoleSetModel roleSet, Collection<? extends ResourceRequest> resources)
            throws NotFoundException {

        // grouping by resource
        Map<PermissionResponseDto, List<ActionEffectDto>> map = new LinkedHashMap<>();
        for (PermissionFlatResponse flatResp : getPermissionsFlattened(roleSet, resources)) {

            PermissionResponseDto resource = permissionMapper.toPermissionResponse(flatResp);
            PermissionResponseDto.ActionEffectDto actionEffect =
                    permissionMapper.toActionEffect(flatResp);

            map.computeIfAbsent(resource, rsc -> new ArrayList<>()).add(actionEffect);
        }

        // set the actionEffects
        map.forEach(PermissionResponseDto::setActionEffects);
        return map.keySet();
    }

    @Override
    public List<? extends PermissionFlatResponse> getPermissionsFlattened(
            UserSubjectRequest userSubject, Collection<? extends ResourceRequest> resources)
            throws NotFoundException {

        RoleSetModel roleSet = getRolesByUserAndScope(userSubject, userSubject);

        return getPermissionsFlattened(roleSet, resources);
    }

    @Override
    public List<? extends PermissionFlatResponse> getPermissionsFlattened(
            RoleSubjectRequest roleSubject, Collection<? extends ResourceRequest> resources)
            throws NotFoundException {

        return getPermissionsFlattened((RoleSetModel) roleSubject, resources);
    }

    private List<? extends PermissionFlatResponse> getPermissionsFlattened(
            RoleSetModel roleSet, Collection<? extends ResourceRequest> resources)
            throws NotFoundException {

        List<PermissionFlatResponseDto> flatResponseList =
                createFlatResponseList(roleSet, resources);

        resolvePermissionsEffects(flatResponseList);

        return flatResponseList;
    }

    @Override
    public Collection<? extends PermissionFlatResponse> getPermissionsFlattened(
            PermissionFlatRequestByUser request) throws NotFoundException {

        RoleSetModel roleSetModel = getRolesByUserAndScope(request, request);

        List<PermissionFlatResponse> resultList = new ArrayList<>();
        for (String roleKey : roleSetModel.getRoleKeys()) {

            PermissionFlatRequestByRoleDto reqByRole =
                    permissionMapper.toPermissionFlatRequestByRoleDto(request);
            reqByRole.setRoleKey(roleKey);

            Collection<? extends PermissionFlatResponse> resp = getPermissionsFlattened(reqByRole);
            resultList.addAll(resp);
        }

        return resultList;
    }

    @Override
    public Collection<? extends PermissionFlatResponse> getPermissionsFlattened(
            PermissionFlatRequestByRole flatReq) throws NotFoundException {

        List<PermissionFlatResponseDto> flatResponse = createFlatResponseList(flatReq);

        resolvePermissionsEffects(flatResponse);

        return flatResponse;
    }

    private void resolvePermissionsEffects(Collection<PermissionFlatResponseDto> flatResponseList)
            throws PolicyNotFoundException {

        Map<PermissionFlatResponseDto, PolicyItemKeyDto> policyItemKeyMapByFlatResp =
                flatResponseList.stream()
                        .collect(
                                Collectors.toMap(
                                        Function.identity(), permissionMapper::toPolicyItemKey));

        // find PolicyItems and map the permission Effect by PolicyItemKey
        Map<PolicyItemKeyDto, Effect> policyMap =
                policyItemProvider
                        .findAllPoliciesByKeys(policyItemKeyMapByFlatResp.values())
                        .collect(
                                Collectors.toMap(
                                        permissionMapper::toPolicyItemKey,
                                        PolicyItemModel::getEffect));

        HashMap<PermissionFlatResponseDto, PolicyItemKey> policyNotFoundMap =
                new HashMap<PermissionFlatResponseDto, PolicyItemKey>(0);

        // set effect from policy data
        policyItemKeyMapByFlatResp.forEach(
                (flatResp, policyItemKey) -> {
                    // get the Effect from policy
                    Effect effect = policyMap.get(policyItemKey);
                    if (effect == null) {
                        policyNotFoundMap.put(flatResp, policyItemKey);
                    } else {
                        // set the permission Effect to the response
                        flatResp.setEffect(effect);
                        log.debug("Effect set to {} for {}", effect, flatResp);
                    }
                });

        // handle policy not found
        if (!policyNotFoundMap.isEmpty()) {
            switch (dataNotFoundStrategy) {
                case THROW_EXCEPTION:
                    throw new PolicyNotFoundException(policyNotFoundMap.values());
                case RETURN_PERMISSION_DENY:
                default:
                    {
                        Effect effect = Effect.DENY;
                        for (PermissionFlatResponseDto flatResp : policyNotFoundMap.keySet()) {
                            flatResp.setEffect(effect);
                            log.debug(
                                    "Effect set to {} because no policy found for {}",
                                    effect,
                                    flatResp);
                        }
                    }
            }
        }
    }

    private RoleSetModel getRolesByUserAndScope(UserModel user, ScopeModel scope)
            throws RoleByUserAndScopeNotFoundException {

        String scopeKey = resolveBlankRbacScopeWithDefault(scope);

        // get all roles from user's scope DB
        Set<String> roleKeys =
                roleProvider
                        .findRolesByUserAndScope(user, () -> scopeKey)
                        .map(RoleModel::getRoleKey)
                        .collect(Collectors.toSet());

        if (roleKeys.isEmpty()) {
            throw new RoleByUserAndScopeNotFoundException(user.getUserKey(), scopeKey);
        }

        return () -> roleKeys;
    }

    private List<PermissionFlatResponseDto> createFlatResponseList(
            PermissionFlatRequestByRole flatReq) throws NotFoundException {

        if (dataNotFoundStrategy == DataNotFoundStrategy.THROW_EXCEPTION) {

            dataExistenceValidator.validateRole(flatReq);

            dataExistenceValidator.validateScope(flatReq);

            if (!"*".equals(flatReq.getActionName())) {
                dataExistenceValidator.validateResource(
                        flatReq, () -> Collections.singleton(flatReq.getActionName()));
            }
        }

        RoleSetModel roleSet = () -> Collections.singleton(flatReq.getRoleKey());

        ArrayList<PermissionFlatResponseDto> resultList =
                new ArrayList<PermissionFlatResponseDto>();

        for (String roleKey : roleSet.getRoleKeys()) {

            String scopeKey = resolveBlankRbacScopeWithDefault(flatReq);

            Set<String> actionNames =
                    resolveActionNameWithWildcardSupport(
                            flatReq, () -> Collections.singleton(flatReq.getActionName()));

            for (String actionName : actionNames) {
                PermissionFlatResponseDto flatResponse =
                        PermissionFlatResponseDto.builder()
                                .roleKey(roleKey)
                                .scopeKey(scopeKey)
                                .resourceTypeKey(flatReq.getResourceTypeKey())
                                .actionName(actionName)
                                .resourceId(flatReq.getResourceId())
                                .build();

                resultList.add(flatResponse);
            }
        }
        return resultList;
    }

    private List<PermissionFlatResponseDto> createFlatResponseList(
            RoleSetModel roleSet, Collection<? extends ResourceRequest> resources)
            throws NotFoundException {

        if (dataNotFoundStrategy == DataNotFoundStrategy.THROW_EXCEPTION) {

            dataExistenceValidator.validateRole(roleSet);

            dataExistenceValidator.validateScope(resources);

            for (ResourceRequest resource : resources) {
                dataExistenceValidator.validateResource(
                        resource, (ResourceActionSetModel) resource);
            }
        }

        ArrayList<PermissionFlatResponseDto> resultList =
                new ArrayList<PermissionFlatResponseDto>();

        for (String roleKey : roleSet.getRoleKeys()) {
            for (ResourceRequest resource : resources) {

                String scopeKey = resolveBlankRbacScopeWithDefault(resource);

                Set<String> actionNames = resolveActionNameWithWildcardSupport(resource, resource);

                for (String actionName : actionNames) {
                    PermissionFlatResponseDto flatResponse =
                            PermissionFlatResponseDto.builder()
                                    .roleKey(roleKey)
                                    .scopeKey(scopeKey)
                                    .resourceTypeKey(resource.getResourceTypeKey())
                                    .actionName(actionName)
                                    .build();

                    resultList.add(flatResponse);
                }
            }
        }
        return resultList;
    }

    // resolve blank scope
    private String resolveBlankRbacScopeWithDefault(ScopeModel scopeModel) {
        String scopeKey = scopeModel.getScopeKey();
        if (StringUtils.isBlank(scopeKey)) {
            return scopeProvider.getDefaultScopeRbac().getScopeKey();
        }
        return scopeKey;
    }

    // resolve wildcard action
    private Set<String> resolveActionNameWithWildcardSupport(
            ResourceTypeModel type, ResourceActionSetModel actionSet) {
        Set<String> actionNames = actionSet.getActionNames();
        if (actionNames.contains("*")) {

            Set<String> oldActionNames = actionNames;

            // generate actionNames from wildcard
            actionNames =
                    resourceActionProvider
                            .findActionsByType(type)
                            .map(ResourceActionModel::getActionName)
                            .collect(Collectors.toSet());

            if (oldActionNames.size() > 1) {
                log.debug(
                        "Wildcard present in action names. The following actions will be ignored: {}",
                        oldActionNames);
            }
        }
        return actionNames;
    }
}
