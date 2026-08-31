package com.hmg.role.rbac.permission;

import static com.hmg.role.rbac.permission.dto.PermissionRequestDto.PermissionRequestUserDto;
import static com.hmg.role.rbac.permission.dto.PermissionRequestDto.ResourceActionsDto;
import static com.hmg.role.rbac.permission.dto.PermissionResponseDto.ActionEffectDto;

import com.hmg.role.rbac.permission.dto.PermissionFlattenedResponseDto;
import com.hmg.role.rbac.permission.dto.PermissionRequestDto;
import com.hmg.role.rbac.permission.dto.PermissionResponseDto;
import com.hmg.role.rbac.permission.exceptions.NullPermissionRequestDtoException;
import com.hmg.role.rbac.permission.exceptions.RoleOrUserKeyIsRequiredException;
import com.hmg.role.rbac.permission.interfaces.PermissionService;
import com.hmg.role.rbac.policy.enums.Effect;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import com.hmg.role.rbac.policy.policyitem.PolicyItemRepository;
import com.hmg.role.rbac.resourceaction.ResourceActionRepository;
import com.hmg.role.rbac.role.exceptions.RoleNotFoundException;
import com.hmg.role.rbac.user.User;
import com.hmg.role.rbac.user.UserRepository;
import com.hmg.role.rbac.user.exceptions.UserNotFoundException;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.Constants;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final PolicyItemRepository policyItemRepository;
    private final UserRepository userRepository;
    private final ResourceActionRepository resourceActionRepository;

    private final PermissionMapper permissionMapper;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    public ListResponseDto<PermissionResponseDto> getAllPermissions(PermissionRequestDto request) {

        var permissionResponseDtos = streamPermissionChecking(request).toList();

        return ListResponseDto.create(permissionResponseDtos);
    }

    public ListResponseDto<PermissionFlattenedResponseDto> getAllPermissionsFlattened(
            PermissionRequestDto request) {

        var permissionFlattenedResponseDtos =
                streamPermissionChecking(request)
                        .flatMap(permissionMapper::toPermissionFlattenedResponseDtoStream)
                        .toList();

        return ListResponseDto.create(permissionFlattenedResponseDtos);
    }

    private Stream<PermissionResponseDto> streamPermissionChecking(PermissionRequestDto request) {

        var roles = getRoles(request.user());
        var userScopeKey = getUserScopeKey(request.user());
        var userScopeRoles = new UserScopeRoles(userScopeKey, roles);

        // find All PolicyItems Broadly
        var unfilteredPolicyItems = findAllPolicyItemsBroadly(request, roles);

        return request.resources().stream()
                .map(Resource::create)
                .map(res -> new FlattenPermissionRequest(userScopeRoles, res))
                .map(flattenReq -> getSingleResourcePermission(flattenReq, unfilteredPolicyItems));
    }

    private PermissionResponseDto getSingleResourcePermission(
            FlattenPermissionRequest request, UnfilteredPolicyItems unfilteredPolicyItems) {

        // specified Permissions
        var specifiedPermissions = findAllSpecifiedPermissions(request, unfilteredPolicyItems);

        // find all diff between the operation request with the specified permissions
        var unspecifiedPermissions =
                findAllUnspecifiedPermissions(
                        request, request.userScopeRoles.roles, specifiedPermissions);

        // join all the actionEffects
        var actionEffects = specifiedPermissions;
        actionEffects.addAll(unspecifiedPermissions);

        var resourceDto =
                permissionMapper.toResourceResponseDto(request.resource.source.resource());

        return new PermissionResponseDto(resourceDto, actionEffects);
    }

    /** retrieves a bulk of policy items that may include unrelated or imprecise data */
    private UnfilteredPolicyItems findAllPolicyItemsBroadly(
            PermissionRequestDto request, List<String> roles) {

        var types = new HashSet<String>();
        var scopes = new HashSet<String>();
        var actions = new HashSet<String>();

        var resourceTypesForWildcardActions = new HashSet<String>();

        for (var resourceActionList : request.resources()) {
            if (hasWildcard(resourceActionList.actions())) {
                resourceTypesForWildcardActions.add(resourceActionList.resource().type());

                var resourceScope = getScopeKeyOrDefault(resourceActionList.resource().scope());
                scopes.add(resourceScope);
            } else {
                var resource = resourceActionList.resource();
                types.add(resource.type());
                actions.addAll(resourceActionList.actions());

                // get scope from resource payload request
                var resourceScope = getScopeKeyOrDefault(resource.scope());
                scopes.add(resourceScope);
            }
        }

        var projectData = authorRequestScope.getProject();

        // translate wildcard to get all actions
        var resourceActionsForWildcardFromDb =
                resourceActionRepository.findAllByResourceTypeKeyIn(
                        resourceTypesForWildcardActions, projectData);
        for (var resourceAction : resourceActionsForWildcardFromDb) {
            types.add(resourceAction.getResourceType().getKey());
            actions.add(resourceAction.getActionName());
        }

        var list =
                policyItemRepository.findAllByResourceActionsAndScopesAndRolesAndProject(
                        types, actions, scopes, roles, projectData);

        return new UnfilteredPolicyItems(list);
    }

    private boolean hasWildcard(Collection<String> list) {
        for (var s : list) {
            if (Constants.WILDCARD.equals(s)) {
                return true;
            }
        }
        return false;
    }

    private record UnfilteredPolicyItems(List<PolicyItem> list) {}

    /** this function allows checking the policy's scope, if not match then deny. */
    private List<ActionEffectDto> findAllSpecifiedPermissions(
            FlattenPermissionRequest request, UnfilteredPolicyItems unfilteredPolicyItems) {

        var specifiedPermissions = new ArrayList<ActionEffectDto>();

        for (var policyItemCandidate : unfilteredPolicyItems.list) {

            if (isRelevantPolicyItem(policyItemCandidate, request) == false) {
                // ignore if this is not relevant policy
                continue;
            }

            var actionEffectDto = permissionMapper.toActionEffectDto(policyItemCandidate);

            specifiedPermissions.add(actionEffectDto);
        }

        return specifiedPermissions;
    }

    // filter if this policyItem is relevant with the permission request
    // match by: resourceTypeKey, String scopeKey, String roleKey, String actionName
    private boolean isRelevantPolicyItem(PolicyItem policyItem, FlattenPermissionRequest request) {

        request.resource
                .actions()
                // warned by sonarlint
                // .strip() handles all whitespaces and non-breaking whitespaces, equivalent to the
                // former regex
                .replaceAll(s -> s == null ? null : s.strip());

        var actions = request.resource.actions();

        if (!request.resource.type.equals(
                policyItem.getResourceAction().getResourceType().getKey())) {
            return false;
        }

        var reqResourceScope = getScopeKeyOrDefault(request.resource.scope);
        var reqUserScope = getUserScopeKey(request);
        var dbPolicyItemScope = policyItem.getScope().getKey();
        // all the three scopeKey equals
        var allScopeEquals =
                StringUtils.equals(reqResourceScope, reqUserScope)
                        && StringUtils.equals(reqResourceScope, dbPolicyItemScope);
        if (!allScopeEquals) {
            return false;
        }

        if (!request.userScopeRoles.roles.contains(policyItem.getRole().getKey())) {
            return false;
        }

        if (!actions.contains(policyItem.getResourceAction().getActionName())
                && !actions.contains(Constants.WILDCARD)) {
            return false;
        }

        return true;
    }

    /** this function allows checking the policy's scope, if not match then deny. */
    private List<ActionEffectDto> findAllSpecifiedPermissions_original(
            FlattenPermissionRequest request, List<PolicyItem> policyItemsBroadly) {

        String userScope = request.userScopeRoles.scope;

        var specifiedPermissions = new ArrayList<ActionEffectDto>();

        for (var policyItemEntity : policyItemsBroadly) {

            var policyScope = policyItemEntity.getScope().getKey();

            var actionEffectDto = permissionMapper.toActionEffectDto(policyItemEntity);

            if (!StringUtils.equals(policyScope, userScope)) {
                log.debug(
                        "policy's scope does not match the request's scope, policyItemId: {}",
                        policyItemEntity.getId());
                actionEffectDto.setEffect(Effect.DENY);
            }

            specifiedPermissions.add(actionEffectDto);
        }

        return specifiedPermissions;
    }

    /**
     * return all denied permissions for the request that has no specified/defined policy
     *
     * <p>this function will compare the diff between the request with the specified permissions,
     * and then all the diff will result deny effect
     */
    private List<ActionEffectDto> findAllUnspecifiedPermissions(
            FlattenPermissionRequest request,
            List<String> roles,
            Collection<ActionEffectDto> specifiedPermissions) {

        var specifiedPermissionMapByRole =
                specifiedPermissions.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ActionEffectDto::getRole, Collectors.toList()));

        var unspecifiedPermissions = new ArrayList<ActionEffectDto>();

        for (var role : roles) {

            var streamOfRequestedAction =
                    request.resource.actions().stream().filter(f -> !f.equals(Constants.WILDCARD));

            var specifiedPermissionsByRole = specifiedPermissionMapByRole.get(role);

            if (specifiedPermissionsByRole != null) {

                // this role has a specified permission
                // then filter the unspecified action

                var specifiedActionNames =
                        specifiedPermissionsByRole.stream()
                                .map(ActionEffectDto::getAction)
                                .toList();

                streamOfRequestedAction =
                        streamOfRequestedAction.filter(a -> !specifiedActionNames.contains(a));
            }
            // this role has no specified permission,
            // then all the actions are unspecified as well

            // map all the unspecified permissions as DENY
            // and then collect to unspecifiedPermissions
            streamOfRequestedAction
                    .map(action -> new ActionEffectDto(role, action, Effect.DENY))
                    .forEach(unspecifiedPermissions::add);
        }

        return unspecifiedPermissions;
    }

    private String getUserScopeKey(PermissionRequestUserDto permissionRequestUserDto) {

        // get scope from payload request
        String userScope = permissionRequestUserDto.scope();
        return getScopeKeyOrDefault(userScope);
    }

    private String getUserScopeKey(FlattenPermissionRequest dto) {
        return getScopeKeyOrDefault(dto.userScopeRoles.scope);
    }

    private String getScopeKeyOrDefault(String scopeKey) {
        if (StringUtils.isBlank(scopeKey)) {
            // if payload request has no scope -> get the default scope
            return authorRequestScope.getDefaultScopeRbac().get().getKey();
        }
        return scopeKey;
    }

    private List<String> getRoles(PermissionRequestUserDto permissionRequestUserDto) {
        // to solve sonar findings
        if (permissionRequestUserDto == null) {
            throw new NullPermissionRequestDtoException();
        }

        var userDtoOpt = Optional.of(permissionRequestUserDto);

        var userKeyOpt =
                userDtoOpt.map(PermissionRequestUserDto::key).filter(StringUtils::isNotBlank);

        var rolesOpt =
                userDtoOpt.map(PermissionRequestUserDto::roles).filter(CollectionUtils::isNotEmpty);

        List<String> roles;

        if (rolesOpt.isEmpty()) {
            if (userKeyOpt.isEmpty()) {
                // no role & user key is provided from payload request
                throw new RoleOrUserKeyIsRequiredException();
            }

            // get user from db
            var userEntity =
                    userRepository.findWithScopeRolesByUserKey(
                            userKeyOpt.get(), authorRequestScope.getProject());

            if (userEntity.isEmpty()) {
                throw new UserNotFoundException();
            }
            // get all roles from user's scope DB
            roles =
                    getRoleListByUserAndScopeKey(
                            userEntity.get(), permissionRequestUserDto.scope());

            if (roles.isEmpty()) {
                throw new RoleNotFoundException();
            }

        } else {
            // get roles from payload
            roles = rolesOpt.get();
            if (roles.isEmpty()) {
                // no role is provided from payload request
                throw new RoleOrUserKeyIsRequiredException();
            }
        }

        return roles;
    }

    private List<String> getRoleListByUserAndScopeKey(User user, String scopeKey) {
        // get user's scope
        String userScope = getScopeKeyOrDefault(scopeKey);

        // get all roles from user's scope DB
        return user.getScopedRoles().stream()
                .filter(scopedRole -> scopedRole.getScope().getKey().equals(userScope))
                .map(scopedRole -> scopedRole.getRole().getKey())
                .toList();
    }

    private record FlattenPermissionRequest(UserScopeRoles userScopeRoles, Resource resource) {}

    private record UserScopeRoles(String scope, List<String> roles) {}

    private record Resource(
            String type, String scope, List<String> actions, ResourceActionsDto source) {
        static Resource create(ResourceActionsDto dto) {
            return new Resource(dto.resource().type(), dto.resource().scope(), dto.actions(), dto);
        }
    }
}
