package com.hmg.role.common.config;

import com.hmg.role.admin.project.ProjectRepository;
import com.hmg.role.rbac.permission.dto.PermissionFlattenedResponseDto;
import com.hmg.role.rbac.permission.dto.PermissionRequestDto;
import com.hmg.role.rbac.policy.policyitem.PolicyItemRepository;
import com.hmg.role.rbac.resourceaction.ResourceActionRepository;
import com.hmg.role.rbac.resourcetype.ResourceTypeRepository;
import com.hmg.role.rbac.role.RoleRepository;
import com.hmg.role.rbac.scope.ScopeRepository;
import com.hmg.role.rbac.userscoperole.UserScopeRoleRepository;
import com.hmg.role.sdk.common.exception.HmgRoleException;
import com.hmg.role.sdk.common.util.CollectionUtils;
import com.hmg.role.sdk.rbac.permission.*;
import com.hmg.role.sdk.rbac.permission.DataExistenceValidator;
import com.hmg.role.sdk.rbac.permission.DataExistenceValidatorImpl;
import com.hmg.role.sdk.rbac.permission.DataNotFoundStrategy;
import com.hmg.role.sdk.rbac.permission.PermissionService;
import com.hmg.role.sdk.rbac.permission.PermissionServiceImpl;
import com.hmg.role.sdk.rbac.permission.dto.*;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponse;
import com.hmg.role.sdk.rbac.permission.dto.PermissionResponse;
import com.hmg.role.sdk.rbac.permission.dto.ResourceRequestDto;
import com.hmg.role.sdk.rbac.permission.dto.RoleSubjectRequest;
import com.hmg.role.sdk.rbac.permission.dto.UserSubjectRequest;
import com.hmg.role.sdk.rbac.permission.dto.UserSubjectRequestDto;
import com.hmg.role.sdk.rbac.permission.model.*;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionSetModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.model.UserModel;
import com.hmg.role.sdk.rbac.permission.spi.*;
import com.hmg.role.sdk.rbac.permission.spi.PolicyItemProvider;
import com.hmg.role.sdk.rbac.permission.spi.ResourceActionProvider;
import com.hmg.role.sdk.rbac.permission.spi.ResourceTypeProvider;
import com.hmg.role.sdk.rbac.permission.spi.RoleProvider;
import com.hmg.role.sdk.rbac.permission.spi.ScopeProvider;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.exceptions.GlobalException;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@ConditionalOnProperty(name = "hmg-role.sdk.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class HmgRoleSdkConfig {

    private final RoleRepository roleRepository;
    private final UserScopeRoleRepository userScopeRoleRepository;
    private final ProjectRepository projectRepository;
    private final ScopeRepository scopeRepository;
    private final PolicyItemRepository policyItemRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ResourceActionRepository resourceActionRepository;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope requestScope;

    @Value("${hmg-role.sdk.dataNotFoundStrategy:RETURN_PERMISSION_DENY}")
    private DataNotFoundStrategy dataNotFoundStrategy;

    @Bean
    HmgRoleSdkMapper sdkMapper() {
        return new HmgRoleSdkMapperImpl();
    }

    @PostConstruct
    void init() {
        log.info("HmgRoleSdkConfig is active");
    }

    @Bean
    @Primary
    com.hmg.role.rbac.permission.interfaces.PermissionService permissionServiceApi(
            PermissionService sdkService, HmgRoleSdkMapper sdkMapper) {
        return new com.hmg.role.rbac.permission.interfaces.PermissionService() {

            List<ResourceRequestDto> toResourceRequestDtos(PermissionRequestDto apiReq) {
                return apiReq.resources().stream().map(sdkMapper::toSdkResourceRequestDto).toList();
            }

            @Transactional
            @Override
            public ListResponseDto<com.hmg.role.rbac.permission.dto.PermissionResponseDto>
                    getAllPermissions(PermissionRequestDto apiReq) {
                log.debug("getAllPermissions request={}", apiReq);

                List<ResourceRequestDto> resourceRequestDtos = toResourceRequestDtos(apiReq);

                Collection<? extends PermissionResponse> sdkResponses;
                try {
                    if (CollectionUtils.isNotEmpty(apiReq.user().roles())) {
                        RoleSubjectRequest roleSubjectRequest =
                                () -> new HashSet<>(apiReq.user().roles());

                        sdkResponses =
                                sdkService.getPermissions(roleSubjectRequest, resourceRequestDtos);

                    } else {

                        UserSubjectRequest userSubjectRequest =
                                sdkMapper.toSdkUserSubjectRequest(apiReq.user());

                        sdkResponses =
                                sdkService.getPermissions(userSubjectRequest, resourceRequestDtos);
                    }
                } catch (HmgRoleException e) {
                    throw new GlobalException(
                            HttpStatus.valueOf(e.getStatusCode()), e.getMessage(), e);
                }

                var dtoRespList =
                        sdkResponses.stream().map(sdkMapper::toPermissionResponseDto).toList();
                return ListResponseDto.create(dtoRespList);
            }

            @Transactional
            @Override
            public ListResponseDto<PermissionFlattenedResponseDto> getAllPermissionsFlattened(
                    PermissionRequestDto apiReq) {
                log.debug("getAllPermissionsFlattened request={}", apiReq);

                var resourceRequestDtos = toResourceRequestDtos(apiReq);

                Collection<? extends PermissionFlatResponse> sdkResponses;
                try {
                    if (CollectionUtils.isNotEmpty(apiReq.user().roles())) {
                        RoleSubjectRequest roleSubjectRequest =
                                () -> new HashSet<>(apiReq.user().roles());

                        sdkResponses =
                                sdkService.getPermissionsFlattened(
                                        roleSubjectRequest, resourceRequestDtos);

                    } else {

                        UserSubjectRequest userSubjectRequest =
                                UserSubjectRequestDto.builder()
                                        .scopeKey(apiReq.user().scope())
                                        .userKey(apiReq.user().key())
                                        .build();

                        sdkResponses =
                                sdkService.getPermissionsFlattened(
                                        userSubjectRequest, resourceRequestDtos);
                    }
                } catch (HmgRoleException e) {
                    throw new GlobalException(
                            HttpStatus.valueOf(e.getStatusCode()), e.getMessage(), e);
                }
                var dtoRespList =
                        sdkResponses.stream()
                                .map(sdkMapper::permissionFlattenedResponseDto)
                                .toList();
                return ListResponseDto.create(dtoRespList);
            }
        };
    }

    @Bean
    PermissionService permissionServiceSdk(
            RoleProvider roleProvider,
            ScopeProvider scopeProvider,
            PolicyItemProvider policyItemProvider,
            ResourceActionProvider resourceActionProvider,
            DataExistenceValidator dataExistenceValidator) {
        log.info("PermissionService - dataNotFoundStrategy={}", dataNotFoundStrategy);
        return PermissionServiceImpl.builder()
                .roleProvider(roleProvider)
                .scopeProvider(scopeProvider)
                .policyItemProvider(policyItemProvider)
                .resourceActionProvider(resourceActionProvider)
                .dataExistenceValidator(dataExistenceValidator)
                .dataNotFoundStrategy(dataNotFoundStrategy)
                .build();
    }

    @Bean
    DataExistenceValidator dataExistenceValidator(
            RoleProvider roleProvider,
            ScopeProvider scopeProvider,
            ResourceActionProvider resourceActionProvider) {
        return DataExistenceValidatorImpl.builder()
                .roleProvider(roleProvider)
                .scopeProvider(scopeProvider)
                .resourceActionProvider(resourceActionProvider)
                .build();
    }

    @Bean
    ResourceTypeProvider resourceTypeProvider() {
        return resourceTypeKeys ->
                resourceTypeRepository
                        .findAllByKeyInAndProjectAndDeletedFalse(
                                resourceTypeKeys, requestScope.getProject())
                        .stream()
                        .map(rscType -> (ResourceTypeModel) rscType::getKey);
    }

    @Bean
    ResourceActionProvider resourceActionProvider() {
        return new ResourceActionProvider() {
            @NotNull
            @Override
            public Stream<ResourceActionModel> findActionsByTypeAndNames(
                    @NotNull ResourceTypeModel resourceType,
                    @NotNull ResourceActionSetModel actionNames) {

                return resourceActionRepository
                        .findAllByResourceTypeKey_AndActionNameInAndResourceTypeProjectAndDeletedIsFalse(
                                resourceType.getResourceTypeKey(),
                                actionNames.getActionNames(),
                                requestScope.getProject())
                        .map(a -> a::getActionName);
            }

            @NotNull
            @Override
            public Stream<ResourceActionModel> findActionsByType(
                    @NotNull ResourceTypeModel resourceType) {
                return resourceActionRepository
                        .findAllByResourceTypeKeyAndResourceTypeProjectAndDeletedIsFalse(
                                resourceType.getResourceTypeKey(), requestScope.getProject())
                        .map(a -> a::getActionName);
            }
        };
    }

    @Bean
    ScopeProvider scopeProvider() {
        return new ScopeProvider() {
            @NotNull
            @Override
            public ScopeModel getDefaultScopeRbac() {
                return () ->
                        projectRepository.getDefaultScopeRbac(requestScope.getProject()).getKey();
            }

            @NotNull
            @Override
            public Stream<ScopeModel> findScopesByKeys(@NotNull Collection<String> scopeKeys) {
                return scopeRepository
                        .findByKeyInAndProjectAndDeletedFalse(scopeKeys, requestScope.getProject())
                        .stream()
                        .map(scope -> scope::getKey);
            }
        };
    }

    @Bean
    RoleProvider roleProvider() {
        return new RoleProvider() {
            @NotNull
            @Override
            public Stream<RoleModel> findRolesByUserAndScope(
                    @NotNull UserModel user, @NotNull ScopeModel scope) {
                return userScopeRoleRepository
                        .findRolesByUserAndScope(
                                user.getUserKey(), scope.getScopeKey(), requestScope.getProject())
                        .map(role -> role::getKey);
            }

            @NotNull
            @Override
            public Stream<RoleModel> findRolesByKeys(@NotNull Collection<String> roleKeys) {
                return roleRepository
                        .findByKeyInAndProjectAndDeletedFalse(roleKeys, requestScope.getProject())
                        .stream()
                        .map(role -> role::getKey);
            }
        };
    }

    @Bean
    PolicyItemProvider policyItemProvider(HmgRoleSdkMapper sdkMapper) {
        return policyItemKeys -> {
            var resourceTypeKeys =
                    policyItemKeys.stream()
                            .map(ResourceTypeModel::getResourceTypeKey)
                            .collect(Collectors.toSet());

            var actionNames =
                    policyItemKeys.stream()
                            .map(ResourceActionModel::getActionName)
                            .collect(Collectors.toSet());

            var roleKeys =
                    policyItemKeys.stream().map(RoleModel::getRoleKey).collect(Collectors.toSet());

            var scopeKeys =
                    policyItemKeys.stream()
                            .map(ScopeModel::getScopeKey)
                            .collect(Collectors.toSet());

            var policyItemList =
                    policyItemRepository
                            .findFetchAllFilterByResourceTypeKeysAndActionsAndRolesAndScopes(
                                    resourceTypeKeys,
                                    actionNames,
                                    roleKeys,
                                    scopeKeys,
                                    requestScope.getProject());

            return policyItemList.stream().map(sdkMapper::toSdkPolicyItemModel);
        };
    }
}
