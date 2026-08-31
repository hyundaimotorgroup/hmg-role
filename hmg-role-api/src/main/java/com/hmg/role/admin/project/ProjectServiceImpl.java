package com.hmg.role.admin.project;

import static com.hmg.role.util.Constants.DEFAULT_SCOPE_KEY;
import static com.hmg.role.util.Constants.DELETED;
import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;

import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.abac.scope.AbacScopeRepository;
import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.admin.project.dto.CreateProjectDto;
import com.hmg.role.admin.project.dto.DeleteBulkProjectDto;
import com.hmg.role.admin.project.dto.ProjectDto;
import com.hmg.role.admin.project.dto.UpdateBulkProjectDto;
import com.hmg.role.admin.project.exceptions.ProjectAlreadyExistException;
import com.hmg.role.admin.project.exceptions.ProjectNotFoundException;
import com.hmg.role.admin.project.interfaces.ProjectService;
import com.hmg.role.common.keymanagement.ProjectEncryptionKeyService;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.scope.ScopeRepository;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestWithKeywordDto;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    // TODO remove these direct scope repository calls and use the service
    // TODO for code cleanliness and concurrency control
    private final ScopeRepository scopeRepository;
    private final AbacScopeRepository abacScopeRepository;
    private final ProjectMapper projectMapper;
    private final ProjectEncryptionKeyService secretsService;
    private final AuditService auditService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    @Override
    public ProjectDto createProject(CreateProjectDto dto) {
        return createBulkProjects(List.of(dto)).results().getFirst();
    }

    @Override
    public ListResponseDto<ProjectDto> createBulkProjects(List<CreateProjectDto> dtoList) {

        var memberData = authorRequestScope.getMemberKey();

        var projectKeys = dtoList.stream().map(CreateProjectDto::key).toList();

        var existingProjects = projectRepository.findByKeyInAndDeletedFalse(projectKeys);
        if (!existingProjects.isEmpty()) {
            var existingProjectKeys = existingProjects.stream().map(Project::getKey).toList();
            throw new ProjectAlreadyExistException(existingProjectKeys);
        }

        var projects =
                dtoList.stream().map(dto -> projectMapper.toProject(dto, memberData)).toList();
        projects = projectRepository.saveAll(projects);

        // Create both RBAC and ABAC default scopes
        final var allDefaultRbacScopes = saveDefaultRbacScopes(projects, memberData);
        final var allDefaultAbacScopes = saveAllDefaultAbacScopes(projects, memberData);

        for (var p : projects) {
            setProjectDefaultScope(p, allDefaultRbacScopes, allDefaultAbacScopes);
        }

        projects = projectRepository.saveAll(projects);
        auditService.commitAsync(projects);

        var results = projects.stream().map(projectMapper::toProjectDto).toList();

        log.info("Successfully Created Projects: {}", projectKeys);

        return ListResponseDto.create(results);
    }

    @Override
    public ListResponseDto<ProjectDto> getAllProjects(PageRequestWithKeywordDto pageRequestDto) {
        Page<ProjectDto> page;

        if (pageRequestDto.getKeyword() == null) {
            log.info("Keyword is null");
            page =
                    projectRepository
                            .findAllByDeletedFalseOrderByUpdatedAtDesc(pageRequestDto.pageRequest())
                            .map(projectMapper::toProjectDto);
        } else {
            String keyword = pageRequestDto.getKeyword();

            log.info("Search Keyword : {}", keyword);

            page =
                    projectRepository
                            .findAllByKeyAndNameAndDeletedFalse(
                                    keyword, pageRequestDto.pageRequest())
                            .map(projectMapper::toProjectDto);
        }
        return ListResponseDto.create(page);
    }

    @Override
    public ProjectDto getProjectByKey(String projectKey) {
        return projectRepository
                .findByKeyAndDeletedFalse(projectKey)
                .map(projectMapper::toProjectDto)
                .orElseThrow(() -> new ProjectNotFoundException(projectKey));
    }

    @Override
    public ListResponseDto<ProjectDto> updateBulkProjects(List<UpdateBulkProjectDto> dtoList) {

        var memberData = authorRequestScope.getMemberKey();

        var projectKeys = dtoList.stream().map(UpdateBulkProjectDto::key).toList();

        var projectList = projectRepository.findByKeyInAndDeletedFalse(projectKeys);

        var existingProjectKeys = projectList.stream().map(Project::getKey).toList();
        var nonExistentProjectKeys =
                projectKeys.stream()
                        .filter(projectKey -> !existingProjectKeys.contains(projectKey))
                        .toList();

        if (!nonExistentProjectKeys.isEmpty()) {
            throw new ProjectNotFoundException(nonExistentProjectKeys);
        }

        var projectMap =
                projectList.stream().collect(Collectors.toMap(Project::getKey, project -> project));

        List<Project> mappedProjectList =
                dtoList.stream()
                        .map(
                                dto ->
                                        mappingUpdateBulkProjectDtoToProject(
                                                dto, projectMap, memberData))
                        .toList();

        var updatedProjects = projectRepository.saveAll(mappedProjectList);
        auditService.commitAsync(updatedProjects);

        var results = updatedProjects.stream().map(projectMapper::toProjectDto).toList();

        log.info("Successfully Updated Projects: {}", projectKeys);

        return ListResponseDto.create(results);
    }

    private Project mappingUpdateBulkProjectDtoToProject(
            UpdateBulkProjectDto dto, Map<String, Project> projectMap, String memberAdminKey) {

        Project project = projectMap.get(dto.key());

        projectMapper.toProject(project, dto, memberAdminKey);

        return project;
    }

    @Override
    public void deleteProject(String projectKey) {
        deleteProjects(List.of(projectKey));
    }

    @Override
    public void deleteBulkProjects(DeleteBulkProjectDto deleteBulkProjectDto) {
        deleteProjects(deleteBulkProjectDto.keys());
    }

    private void deleteProjects(List<String> keys) {

        var projectList = projectRepository.findByKeyInAndDeletedFalse(keys);

        if (keys.size() != projectList.size()) {
            var existingKeys = projectList.stream().map(Project::getKey).toList();
            var nonExistentKeys = keys.stream().filter(k -> !existingKeys.contains(k)).toList();
            if (!nonExistentKeys.isEmpty()) {
                throw new ProjectNotFoundException(nonExistentKeys);
            }
        }

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        for (var project : projectList) {
            project.setDeleted(true);
            project.setKey(DELETED + "-" + deletedDatetime + "-" + project.getKey());
            secretsService.delete(project.getKey());
        }

        projectRepository.saveAll(projectList);
        auditService.commitAsync(projectList);

        log.info("Successfully Deleted Projects: {}", keys);
    }

    private Map<String, Scope> saveDefaultRbacScopes(List<Project> projects, String memberKey) {
        // saving a list like this is ugly code-wise
        // but performant because it avoids db round-trip
        var allDefaultRbacScopes =
                projects.stream().map(p -> defaultScopeRbac(p, memberKey)).toList();
        allDefaultRbacScopes = scopeRepository.saveAll(allDefaultRbacScopes);
        return allDefaultRbacScopes.stream()
                .collect(Collectors.toMap(k -> k.getProject().getKey(), Function.identity()));
    }

    private Map<String, AbacScope> saveAllDefaultAbacScopes(
            List<Project> projects, String memberKey) {
        var allDefaultAbacScopes =
                projects.stream().map(p -> defaultScopeAbac(p, memberKey)).toList();
        allDefaultAbacScopes = abacScopeRepository.saveAll(allDefaultAbacScopes);
        return allDefaultAbacScopes.stream()
                .collect(Collectors.toMap(k -> k.getProject().getKey(), Function.identity()));
    }

    private Scope defaultScopeRbac(Project project, String memberKey) {
        var now = ZonedDateTime.now();
        var res =
                Scope.builder()
                        .key(DEFAULT_SCOPE_KEY)
                        .name(DEFAULT_SCOPE_KEY)
                        .project(project)
                        .build();

        res.setCreatedAt(now);
        res.setCreatedBy(memberKey);
        res.setUpdatedAt(now);
        res.setUpdatedBy(memberKey);
        return res;
    }

    private AbacScope defaultScopeAbac(Project project, String memberKey) {
        var now = ZonedDateTime.now();
        var abacScope = new AbacScope(null, DEFAULT_SCOPE_KEY, DEFAULT_SCOPE_KEY, false, project);
        abacScope.setCreatedBy(memberKey);
        abacScope.setCreatedAt(now);
        abacScope.setUpdatedBy(memberKey);
        abacScope.setUpdatedAt(now);
        return abacScope;
    }

    private static void setProjectDefaultScope(
            Project project,
            Map<String, Scope> allDefaultRbacScopes,
            Map<String, AbacScope> allDefaultAbacScopes) {
        var rbacDefaultScope =
                Optional.ofNullable(allDefaultRbacScopes.get(project.getKey()))
                        .orElseThrow(() -> new RuntimeException("RBAC default scope not found"));
        project.setDefaultScopeRbac(rbacDefaultScope);
        var abacDefaultScope =
                Optional.ofNullable(allDefaultAbacScopes.get(project.getKey()))
                        .orElseThrow(() -> new RuntimeException("ABAC default scope not found"));
        project.setDefaultScopeAbac(abacDefaultScope);
    }
}
