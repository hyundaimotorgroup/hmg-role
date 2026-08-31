package com.hmg.role.rbac.scope.interfaces;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.scope.dto.CreateScopeDto;
import com.hmg.role.rbac.scope.dto.ScopeDto;
import com.hmg.role.rbac.scope.dto.UpdateScopeDto;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestDto;
import java.util.Collection;
import java.util.List;

public interface ScopeService {
    ScopeDto create(CreateScopeDto createDto);

    ScopeDto getByKey(String scopeKey);

    ListResponseDto<ScopeDto> getAll(PageRequestDto pageRequestDto);

    ScopeDto update(String scopeKey, UpdateScopeDto updatedScope);

    void deleteByKey(String scopeKey);

    void deleteCascadeByKey(String scopeKey);

    void validateScope(String scopeKey, Project projectData);

    void validateScopes(List<String> scopeKey, Project projectData);

    boolean existsScopeKey(String scopeKey, Project project);

    boolean existsScopeName(String name, Project project);

    boolean existsScopeKeys(List<String> scopeKey, Project project);

    List<ScopeDto> getByScopeKeys(List<String> resourcesScope, Project project);

    List<Scope> findByKeysAndThrowIfNotExists(Collection<String> scopeKeys);

    List<Scope> findByScopeKeyInAndProjectAndDeletedFalse(List<String> scopeKeys, Project project);

    default Scope findByKeyAndThrowIfNotExists(String scopeKey) {
        return findByKeysAndThrowIfNotExists(List.of(scopeKey)).getFirst();
    }
}
