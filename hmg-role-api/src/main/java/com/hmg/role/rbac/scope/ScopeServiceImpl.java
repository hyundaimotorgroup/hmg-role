package com.hmg.role.rbac.scope;

import com.hmg.role.abac.scope.AbacScopeRepository;
import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.Policy;
import com.hmg.role.rbac.policy.interfaces.PolicyService;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import com.hmg.role.rbac.policy.policyitem.PolicyItemRepository;
import com.hmg.role.rbac.scope.dto.CreateScopeDto;
import com.hmg.role.rbac.scope.dto.ScopeDto;
import com.hmg.role.rbac.scope.dto.UpdateScopeDto;
import com.hmg.role.rbac.scope.exceptions.ScopeAlreadyExistException;
import com.hmg.role.rbac.scope.exceptions.ScopeBeingUsedException;
import com.hmg.role.rbac.scope.exceptions.ScopeNotFoundException;
import com.hmg.role.rbac.scope.interfaces.ScopeService;
import com.hmg.role.rbac.user.User;
import com.hmg.role.rbac.userscoperole.UserScopeRole;
import com.hmg.role.rbac.userscoperole.UserScopeRoleRepository;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestDto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
// TODO refactor the entire business logic to remove this qualifier
@Qualifier("rbacScopeServiceImpl") // necessary since ABAC scope is basically a copy of this
public class ScopeServiceImpl implements ScopeService {

    private final ScopeRepository scopeRepository;
    private final AbacScopeRepository abacScopeRepository;
    private final UserScopeRoleRepository userScopeRoleRepository;
    private final PolicyItemRepository policyItemRepository;

    private final AuditService auditService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private PolicyService policyService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    private final ScopeMapper scopeMapper;

    @Override
    public ScopeDto create(CreateScopeDto createDto) {

        Project projectData = getProject();

        if (existsScopeKey(createDto.key(), projectData)) {
            throw new ScopeAlreadyExistException(createDto.key());
        }

        if (existsScopeName(createDto.name(), projectData)) {
            throw new ScopeAlreadyExistException(createDto.name());
        }

        Scope scope = scopeMapper.toScope(createDto, projectData);
        // TODO: remove once AuditorAware is safely wired
        String authorKey = authorRequestScope.getMemberKey();
        scope.setCreatedBy(authorKey);
        scope.setUpdatedBy(authorKey);
        var finalScope = scopeRepository.save(scope);
        auditService.commitAsync(finalScope);

        return scopeMapper.toScopeDto(finalScope);
    }

    @Override
    public ScopeDto getByKey(String scopeKey) {
        Project projectData = getProject();

        Scope scope =
                scopeRepository
                        .findByKeyAndProjectAndDeletedFalse(scopeKey, projectData)
                        .orElseThrow(ScopeNotFoundException::new);
        return scopeMapper.toScopeDto(scope);
    }

    @Override
    public ListResponseDto<ScopeDto> getAll(PageRequestDto pageRequestDto) {
        Project projectData = getProject();
        var scopesDtoPage = getRbacScope(pageRequestDto, projectData);
        return ListResponseDto.create(scopesDtoPage);
    }

    @Override
    public ScopeDto update(String scopeKey, UpdateScopeDto updateScopeDto) {
        String updatedName = updateScopeDto.name();

        Project projectData = getProject();

        if (existsScopeName(updatedName, projectData)) {
            throw new ScopeAlreadyExistException(updatedName);
        }

        Scope scope =
                scopeRepository
                        .findByKeyAndProjectAndDeletedIsFalse(scopeKey, projectData)
                        .orElseThrow(ScopeNotFoundException::new);

        if (StringUtils.isNoneBlank(updatedName) && existsScopeName(updatedName, projectData)) {
            throw new ScopeAlreadyExistException(updatedName);
        }

        Scope updateScope = scopeMapper.toScope(scope, updateScopeDto);
        updateScope.setUpdatedBy(authorRequestScope.getMemberKey());
        var updatedScope = scopeRepository.save(updateScope);
        auditService.commitAsync(updatedScope);

        return scopeMapper.toScopeDto(updatedScope);
    }

    @Override
    public void deleteByKey(String scopeKey) {
        deleteByKey(scopeKey, false);
    }

    @Override
    public void deleteCascadeByKey(String scopeKey) {
        deleteByKey(scopeKey, true);
    }

    private void deleteByKey(String scopeKey, boolean cascade) {
        Project projectData = authorRequestScope.getProject();

        // delete based on access model
        var scope =
                scopeRepository
                        .findByKeyAndProjectAndDeletedIsFalse(scopeKey, projectData)
                        .orElseThrow(ScopeNotFoundException::new);

        var userScopeRoleList =
                userScopeRoleRepository.findByScopeAndDeletedFalseAndUserDeletedFalse(scope);
        var policyItemList = policyItemRepository.findByScopeAndPolicyDeletedFalse(scope);

        var isProjectDefaultScope =
                authorRequestScope.getDefaultScopeRbac().get().getKey().equals(scopeKey);

        if (isProjectDefaultScope) {
            throw new ScopeBeingUsedException(scopeKey);
        }

        if (cascade) {
            if (!userScopeRoleList.isEmpty()) {
                log.debug("SoftDelete UserScopeRole by scopeKey: {}", scopeKey);
                userScopeRoleList.forEach(userScopeRole -> userScopeRole.setDeleted(true));
                userScopeRoleRepository.saveAll(userScopeRoleList);
                auditService.commitAsync(userScopeRoleList);
            }
            if (!policyItemList.isEmpty()) {
                log.debug("SoftDelete PolicyItem by scopeKey: {}", scopeKey);
                policyService.deletePolicyItems(policyItemList);
            }
        } else {
            if (!userScopeRoleList.isEmpty() || !policyItemList.isEmpty()) {
                var userKeys =
                        userScopeRoleList.stream()
                                .map(UserScopeRole::getUser)
                                .map(User::getUserKey)
                                .collect(Collectors.toSet());

                var policyKeys =
                        policyItemList.stream()
                                .map(PolicyItem::getPolicy)
                                .map(Policy::getKey)
                                .collect(Collectors.toSet());

                Set<String> projectKeys =
                        // default scope triggers false alarm "always false" in sonar`
                        isProjectDefaultScope ? Set.of(projectData.getKey()) : Set.of(); // NOSONAR

                var conflictDetail =
                        scopeMapper.toDetailedUsageByUsersScopesPolicies(
                                userKeys, policyKeys, projectKeys);
                throw new ScopeBeingUsedException(conflictDetail);
            }
        }

        scope.setDeleted(true);
        scope.setUpdatedBy(authorRequestScope.getMemberKey());
        scopeRepository.save(scope);
        auditService.commitAsync(scope);
        log.info("Successfully Deleted Scopes: {}", scopeKey);
    }

    @Override
    public void validateScope(String scopeKey, Project projectData) {
        if (StringUtils.isNoneBlank(scopeKey)) {
            var isScopedExists = existsScopeKey(scopeKey, projectData);
            if (!isScopedExists) {
                throw new ScopeNotFoundException(scopeKey);
            }
        }
    }

    @Override
    public void validateScopes(List<String> scopeKey, Project projectData) {
        if (!scopeKey.isEmpty()) {
            var isScopeExists = existsScopeKeys(scopeKey, projectData);
            if (!isScopeExists) {
                throw new ScopeNotFoundException(scopeKey);
            }
        }
    }

    @Override
    public boolean existsScopeKey(String scopeKey, Project project) {
        return scopeRepository.existsByKeyAndProjectAndDeletedFalse(scopeKey, project);
    }

    @Override
    public boolean existsScopeName(String name, Project project) {
        return scopeRepository.existsByNameAndProjectAndDeletedFalse(name, project);
    }

    @Override
    public boolean existsScopeKeys(List<String> scopeKeys, Project project) {
        return scopeRepository.existsByKeyInAndProjectAndDeletedFalse(scopeKeys, project);
    }

    @Override
    public List<ScopeDto> getByScopeKeys(List<String> resourcesScope, Project project) {
        var scope = scopeRepository.findByKeyInAndProjectAndDeletedFalse(resourcesScope, project);
        return scope.stream().map(scopeMapper::toScopeDto).toList();
    }

    @Override
    public List<Scope> findByKeysAndThrowIfNotExists(Collection<String> reqScopeKeys) {

        if (reqScopeKeys.isEmpty()) {
            return List.of();
        }

        var project = authorRequestScope.getProject();
        var scopeEntities =
                scopeRepository.findByKeyInAndProjectAndDeletedFalse(reqScopeKeys, project);

        if (scopeEntities.size() != reqScopeKeys.size()) {
            var invalidKeys = new ArrayList<>(reqScopeKeys);
            invalidKeys.removeAll(scopeEntities.stream().map(Scope::getKey).toList());
            throw new ScopeNotFoundException(invalidKeys);
        }

        return scopeEntities;
    }

    @Override
    public List<Scope> findByScopeKeyInAndProjectAndDeletedFalse(
            List<String> scopeKeys, Project project) {
        return scopeRepository.findByKeyInAndProjectAndDeletedFalse(scopeKeys, project);
    }

    private Project getProject() {
        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }
        return projectData;
    }

    private PageImpl<ScopeDto> getRbacScope(PageRequestDto pageRequestDto, Project projectData) {
        Specification<Scope> projectFilter =
                (root, query, cb) -> cb.equal(root.get(Scope.PROP_PROJECT), projectData);
        Specification<Scope> spec =
                projectFilter
                        .and((root, query, cb) -> cb.isFalse(root.get(Scope.PROP_DELETED)))
                        .and(ScopeOrderingSpec.withBucketedOrder());

        var scopes = scopeRepository.findAll(spec, pageRequestDto.pageRequest());

        List<ScopeDto> scopesDto =
                scopes.stream().map(k -> new ScopeDto(k.getKey(), k.getName())).toList();

        var scopesDtoPage =
                new PageImpl<>(scopesDto, pageRequestDto.pageRequest(), scopes.getTotalElements());
        return scopesDtoPage;
    }
}
