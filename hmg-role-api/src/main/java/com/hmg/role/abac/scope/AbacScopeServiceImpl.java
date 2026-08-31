package com.hmg.role.abac.scope;

import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;

import com.hmg.role.abac.policy.policyitem.AbacPolicyItemRepository;
import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.scope.dto.CreateScopeDto;
import com.hmg.role.rbac.scope.dto.ScopeDto;
import com.hmg.role.rbac.scope.dto.UpdateScopeDto;
import com.hmg.role.rbac.scope.exceptions.ScopeAlreadyExistException;
import com.hmg.role.rbac.scope.exceptions.ScopeBeingUsedException;
import com.hmg.role.rbac.scope.exceptions.ScopeNotFoundException;
import com.hmg.role.rbac.scope.interfaces.ScopeService;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestDto;
import com.hmg.role.util.exceptions.BadRequestException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
// TODO refactor the entire business logic to remove this qualifier
@Qualifier("abacScopeServiceImpl") // necessary since ABAC scope is basically a copy of this
public class AbacScopeServiceImpl implements ScopeService {

    private final AbacScopeRepository scopeRepository;
    private final AbacPolicyItemRepository policyItemRepository;

    private final AuditService auditService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    private final AbacScopeMapper scopeMapper;

    @Override
    public ScopeDto create(CreateScopeDto createDto) {

        Project projectData = getProject();
        String userName = authorRequestScope.getMemberKey();

        var existingScope =
                scopeRepository.findByKeyOrNameAndProjectAndDeletedFalse(
                        createDto.key(), createDto.name(), projectData);

        if (existingScope.isPresent()) {
            AbacScope scope = existingScope.get();
            String conflictType =
                    determineScopeConflictType(scope, createDto.key(), createDto.name());
            throw new ScopeAlreadyExistException(createDto.key(), conflictType);
        }

        AbacScope scope = scopeMapper.toScope(createDto, projectData, userName, userName);
        var finalScope = scopeRepository.save(scope);
        // auditService.commitAsync(finalScope); // auditing is out of scope for now,
        // and it
        // breaks the app

        return scopeMapper.toScopeDto(finalScope);
    }

    @Override
    public ScopeDto getByKey(String scopeKey) {
        Project projectData = getProject();

        AbacScope scope =
                scopeRepository
                        .findByKeyAndProjectAndDeletedIsFalse(scopeKey, projectData)
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

        AbacScope scope =
                scopeRepository
                        .findByKeyAndProjectAndDeletedIsFalse(scopeKey, projectData)
                        .orElseThrow(ScopeNotFoundException::new);

        if (StringUtils.isNoneBlank(updatedName) && existsScopeName(updatedName, projectData)) {
            throw new ScopeAlreadyExistException(updatedName);
        }

        var userName = authorRequestScope.getMemberKey();
        AbacScope updateScope = scopeMapper.toScope(scope, updateScopeDto, userName);
        var updatedScope = scopeRepository.save(updateScope);
        // auditService.commitAsync(updatedScope); // auditing is out of scope for now,
        // and it
        // breaks the app

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

        var scope =
                scopeRepository
                        .findByKeyAndProjectAndDeletedIsFalse(scopeKey, projectData)
                        .orElseThrow(ScopeNotFoundException::new);

        var scopeHavePolicy = policyItemRepository.existsByScopeAndProject(scope, projectData);

        var isProjectDefaultScope =
                scopeKey.equals(authorRequestScope.getDefaultScopeAbac().get().getKey());

        if (isProjectDefaultScope) {
            throw new ScopeBeingUsedException(scopeKey);
        }

        if (cascade) {
            if (scopeHavePolicy) {
                log.debug("SoftDelete PolicyItem by scopeKey: {}", scopeKey);
                var policyItemList = policyItemRepository.findByScopeAndPolicyDeletedFalse(scope);
                policyItemList.forEach(policyItem -> policyItem.setDeleted(true));
                policyItemRepository.saveAll(policyItemList);
                // auditService.commitAsync(policyItemList); // auditing is out of scope for
                // now,
                // and it
                // // breaks the app
            }
        } else {
            if (scopeHavePolicy) {
                throw new ScopeBeingUsedException(scopeKey);
            }
        }

        String deletedScopeKey =
                String.format(
                        "deleted-%s-%s",
                        ZonedDateTime.now()
                                .format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT)),
                        scopeKey);
        scope.setKey(deletedScopeKey);
        scope.setDeleted(true);
        scopeRepository.save(scope);

        // auditing is out of scope for now,
        // and it breaks the app
        // auditService.commitAsync(scope);
        log.info("Successfully Deleted Scopes: {}", scopeKey);
    }

    @Override
    public void validateScope(String scopeKey, Project projectData) {
        if (StringUtils.isNotBlank(scopeKey)) {
            var isScopedExists = existsScopeKey(scopeKey, projectData);
            if (!isScopedExists) {
                throw new ScopeNotFoundException(scopeKey);
            }
        } else {
            throw new BadRequestException("Null or blank entry: %s".formatted(scopeKey));
        }
    }

    @Override
    public void validateScopes(List<String> scopeKeys, Project projectData) {
        if (!scopeKeys.isEmpty()) {
            var foundKeys =
                    scopeRepository
                            .findByKeyInAndProjectAndDeletedFalse(scopeKeys, projectData)
                            .stream()
                            .map(AbacScope::getKey)
                            .toList();
            var missingKeys = scopeKeys.stream().filter(k -> !foundKeys.contains(k)).toList();
            if (!missingKeys.isEmpty()) {
                throw new ScopeNotFoundException(missingKeys);
            }
        }
    }

    @Override
    public boolean existsScopeKey(String scopeKey, Project project) {
        var dbData = scopeRepository.findByKeyAndProjectAndDeletedFalse(scopeKey, project);
        return dbData.isPresent();
    }

    @Override
    public boolean existsScopeName(String name, Project project) {
        var dbData = scopeRepository.findByNameAndProjectAndDeletedFalse(name, project);
        return dbData.isPresent();
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
        throw new UnsupportedOperationException(); // not used in ABAC
    }

    @Override
    public List<Scope> findByScopeKeyInAndProjectAndDeletedFalse(
            List<String> scopeKeys, Project project) {
        // no use case for ABAC
        throw new UnsupportedOperationException();
    }

    private Project getProject() {
        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }
        return projectData;
    }

    private PageImpl<ScopeDto> getRbacScope(PageRequestDto pageRequestDto, Project projectData) {
        var scopes =
                scopeRepository.findByProjectAndDeletedIsFalseOrderByNameAsc(
                        projectData, pageRequestDto.pageRequest());

        List<ScopeDto> scopesDto =
                scopes.stream().map(k -> new ScopeDto(k.getKey(), k.getName())).toList();

        var scopesDtoPage =
                new PageImpl<>(scopesDto, pageRequestDto.pageRequest(), scopes.getTotalElements());
        return scopesDtoPage;
    }

    private String determineScopeConflictType(
            AbacScope existingScope, String incomingKey, String incomingName) {
        boolean keyMatch = existingScope.getKey().equals(incomingKey);
        boolean nameMatch = existingScope.getName().equals(incomingName);

        if (keyMatch && nameMatch) {
            return "BOTH";
        } else if (keyMatch) {
            return "KEY";
        } else {
            return "NAME";
        }
    }
}
