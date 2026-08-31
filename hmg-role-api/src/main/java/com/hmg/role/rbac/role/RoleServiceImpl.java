package com.hmg.role.rbac.role;

import static com.hmg.role.util.Constants.DELETED;
import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;
import static com.hmg.role.util.Constants.MAX_500_SIZE;
import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.ProjectMapper;
import com.hmg.role.admin.project.dto.ProjectDto;
import com.hmg.role.rbac.policy.Policy;
import com.hmg.role.rbac.policy.interfaces.PolicyService;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import com.hmg.role.rbac.policy.policyitem.PolicyItemRepository;
import com.hmg.role.rbac.role.dto.CreateRoleDto;
import com.hmg.role.rbac.role.dto.DeleteBulkRoleDto;
import com.hmg.role.rbac.role.dto.RoleDto;
import com.hmg.role.rbac.role.dto.RoleScopeUserRequestDto;
import com.hmg.role.rbac.role.dto.RoleSearchRequestDto;
import com.hmg.role.rbac.role.dto.RoleWithUserCountDto;
import com.hmg.role.rbac.role.dto.UpdateBulkRoleDto;
import com.hmg.role.rbac.role.dto.UpdateRoleDto;
import com.hmg.role.rbac.role.dto.UserConflictWithRoleDto;
import com.hmg.role.rbac.role.enums.Type;
import com.hmg.role.rbac.role.exceptions.RoleAlreadyExistException;
import com.hmg.role.rbac.role.exceptions.RoleBeingUsedException;
import com.hmg.role.rbac.role.exceptions.RoleMaxException;
import com.hmg.role.rbac.role.exceptions.RoleNotFoundException;
import com.hmg.role.rbac.role.exceptions.TooManyRolesException;
import com.hmg.role.rbac.role.interfaces.RoleService;
import com.hmg.role.rbac.role.projections.RoleProjection;
import com.hmg.role.rbac.user.User;
import com.hmg.role.rbac.userscoperole.UserScopeRoleRepository;
import com.hmg.role.rbac.userscoperole.dto.ScopeUserDto;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.exceptions.BadRequestException;
import com.hmg.role.util.exceptions.TypeNotFoundException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserScopeRoleRepository userScopeRoleRepository;
    private final PolicyItemRepository policyItemRepository;
    private final RoleMapper roleMapper;
    private final ProjectMapper projectMapper;
    private final AuditService auditService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private PolicyService policyService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    public RoleDto createRole(CreateRoleDto createRoleDto) {
        String authorKey = authorRequestScope.getMemberKey();

        Project projectData = getCurrentProject();

        if (roleRepository.existsByKeyAndProjectAndDeletedFalse(createRoleDto.key(), projectData)) {
            throw new RoleAlreadyExistException(createRoleDto.key());
        }

        var roleData = roleMapper.toRole(createRoleDto);
        roleData.setProject(projectData);
        roleData.setCreatedBy(authorKey);
        roleData.setUpdatedBy(authorKey);

        var savedRole = roleRepository.save(roleData);
        auditService.commitAsync(savedRole);

        log.info(
                "Successfully Created Role: {} from Project : {}",
                savedRole.getKey(),
                projectData.getKey());

        return roleMapper.toRoleDto(savedRole);
    }

    public ListResponseDto<RoleDto> createBulkRoles(List<CreateRoleDto> createRoleDtos) {

        Project projectData = getCurrentProject();

        // make sure that role count of the project doesn't exceed MAX_LIST_SIZE after insertion
        int countExistingRoleByProject =
                roleRepository.countRolesByProjectAndDeletedFalse(projectData);
        int countTotalRoleByProject = countExistingRoleByProject + createRoleDtos.size();
        if (countTotalRoleByProject > MAX_LIST_SIZE) {
            log.error(
                    "Too many roles for the project. Total count: {}, count limit: {}",
                    countTotalRoleByProject,
                    MAX_500_SIZE);
            throw new TooManyRolesException();
        }

        var roleKeys = createRoleDtos.stream().map(CreateRoleDto::key).toList();

        var roleEntities =
                roleRepository.findRoleByKeyInAndProjectAndDeletedFalse(roleKeys, projectData);

        var existingRoleKeys =
                roleEntities.stream().map(Role::getKey).filter(roleKeys::contains).toList();

        if (!existingRoleKeys.isEmpty()) {
            throw new RoleAlreadyExistException(existingRoleKeys);
        }

        String authorKey = authorRequestScope.getMemberKey();
        var roleDataList =
                createRoleDtos.stream()
                        .map(roleMapper::toRole)
                        .peek(
                                role -> {
                                    role.setProject(projectData);
                                    role.setCreatedBy(authorKey);
                                    role.setUpdatedBy(authorKey);
                                })
                        .toList();

        var savedRoleDataList = roleRepository.saveAll(roleDataList);
        auditService.commitAsync(savedRoleDataList);

        var savedRoleDtoList = savedRoleDataList.stream().map(roleMapper::toRoleDto).toList();

        log.info(
                "Successfully Created Roles: {} from Project: {}",
                existingRoleKeys,
                projectData.getKey());

        return ListResponseDto.create(savedRoleDtoList);
    }

    public ListResponseDto<RoleWithUserCountDto> listRole(RoleSearchRequestDto paginationDto) {

        Project projectData = getCurrentProject();

        var keyword = escapeLike(paginationDto.getKeyword());
        var pageRequest = paginationDto.pageRequest();

        int offset =
                paginationDto.pageRequest().getPageNumber()
                        * paginationDto.pageRequest().getPageSize();

        if (offset >= MAX_LIST_SIZE) {
            throw new RoleMaxException();
        }

        var keywordBlank = StringUtils.isBlank(paginationDto.getKeyword());
        var typeBlank = StringUtils.isBlank(paginationDto.getType());

        if (keywordBlank && typeBlank) {
            var roleEntities =
                    roleRepository.findByProjectAndDeletedFalseOrderByNameAsc(
                            projectData, paginationDto.pageRequest());

            List<String> roleKey = roleEntities.getContent().stream().map(Role::getKey).toList();

            var numberOfUsersByRole =
                    roleRepository.countUsersPerRole(roleKey, projectData).stream()
                            .collect(
                                    Collectors.toMap(
                                            RoleProjection::getKey,
                                            RoleProjection::getCountUsersPerRole));

            var roleDtoList =
                    roleEntities.map(
                            role ->
                                    roleMapper.toRoleWithUserCountDto(
                                            role,
                                            numberOfUsersByRole.getOrDefault(role.getKey(), 0L)));

            return ListResponseDto.create(roleDtoList);
        }

        if (keywordBlank || typeBlank) {
            throw new BadRequestException("Keyword and Type are required");
        }

        try {
            var typeSearch = Type.valueOf(paginationDto.getType().toUpperCase());
            Page<Role> roleEntities =
                    switch (typeSearch) {
                        case NAME ->
                                roleRepository.searchByProjectAndKeywordName(
                                        projectData, keyword, pageRequest);
                        case KEY ->
                                roleRepository.searchByProjectAndKeywordKey(
                                        projectData, keyword, pageRequest);
                        default ->
                                throw new UnsupportedOperationException(
                                        "Search By " + typeSearch + " is not supported");
                    };

            List<String> roleKey = roleEntities.getContent().stream().map(Role::getKey).toList();

            var numberOfUsersByRole =
                    roleRepository.countUsersPerRole(roleKey, projectData).stream()
                            .collect(
                                    Collectors.toMap(
                                            RoleProjection::getKey,
                                            RoleProjection::getCountUsersPerRole));

            var roleDtoList =
                    roleEntities.map(
                            role ->
                                    roleMapper.toRoleWithUserCountDto(
                                            role,
                                            numberOfUsersByRole.getOrDefault(role.getKey(), 0L)));

            return ListResponseDto.create(roleDtoList);

        } catch (IllegalArgumentException e) {
            throw new TypeNotFoundException();
        }
    }

    public RoleDto getRoleByKey(String roleKey) {

        Project projectData = getCurrentProject();

        var roleEntity =
                roleRepository
                        .findByKeyAndProjectAndDeletedFalse(roleKey, projectData)
                        .orElseThrow(() -> new RoleNotFoundException(roleKey));

        return roleMapper.toRoleDto(roleEntity);
    }

    @Override
    public List<Role> findRolesAndThrowIfNotExists(Collection<String> roleKeys) {

        Project project = getCurrentProject();

        var roleEntities = roleRepository.findByKeyInAndProjectAndDeletedFalse(roleKeys, project);

        if (roleEntities.size() != roleKeys.size()) {
            var invalidRoles = new ArrayList<>(roleKeys);
            invalidRoles.removeAll(roleEntities.stream().map(Role::getKey).toList());
            log.debug("invalid roles: {}", invalidRoles);
            throw new RoleNotFoundException(invalidRoles);
        }

        return roleEntities;
    }

    public RoleDto updateRole(String roleKey, UpdateRoleDto updateRoleDto) {

        Project projectData = getCurrentProject();

        var roleEntity =
                roleRepository
                        .findByKeyAndProjectAndDeletedFalse(roleKey, projectData)
                        .orElseThrow(() -> new RoleNotFoundException(roleKey));

        roleMapper.toRole(roleEntity, updateRoleDto);

        String authorKey = authorRequestScope.getMemberKey();
        roleEntity.setUpdatedBy(authorKey);

        var savedRoleData = roleRepository.save(roleEntity);
        auditService.commitAsync(savedRoleData);

        log.info(
                "Successfully Updated Role: {} from Project : {}",
                savedRoleData.getName(),
                projectData.getKey());

        return roleMapper.toRoleDto(savedRoleData);
    }

    public ListResponseDto<RoleDto> updateBulkRoles(List<UpdateBulkRoleDto> updateBulkRoleDtos) {

        Project projectData = getCurrentProject();

        var roleKeys = updateBulkRoleDtos.stream().map(UpdateBulkRoleDto::key).toList();

        var roleEntities = findRolesAndThrowIfNotExists(roleKeys);

        var roleMap = roleEntities.stream().collect(Collectors.toMap(Role::getKey, role -> role));

        List<Role> updatedRoleDataList = new ArrayList<>();

        updateBulkRoleDtos.forEach(
                updateBulkRoleDto -> {
                    var roleData = roleMap.get(updateBulkRoleDto.key());
                    roleMapper.toRole(roleData, updateBulkRoleDto);

                    String authorKey = authorRequestScope.getMemberKey();
                    roleData.setUpdatedBy(authorKey);

                    updatedRoleDataList.add(roleData);
                });

        var savedRoleDataList = roleRepository.saveAll(updatedRoleDataList);
        auditService.commitAsync(savedRoleDataList);

        var savedRoleDtoList = savedRoleDataList.stream().map(roleMapper::toRoleDto).toList();

        log.info("Successfully Updated Roles: {} from Project: {}", roleKeys, projectData.getKey());

        return ListResponseDto.create(savedRoleDtoList);
    }

    public void deleteRole(String roleKey) {
        deleteRoles(List.of(roleKey), false);
    }

    @Override
    public void deleteRoleCascade(String roleKey) {
        deleteRoles(List.of(roleKey), true);
    }

    public void deleteBulkRoles(DeleteBulkRoleDto deleteBulkRoleDto) {

        var roleKeys = deleteBulkRoleDto.keys();
        deleteRoles(roleKeys, false);
    }

    @Override
    public void deleteBulkRolesCascade(DeleteBulkRoleDto deleteBulkRoleDto) {
        var roleKeys = deleteBulkRoleDto.keys();
        deleteRoles(roleKeys, true);
    }

    @Override
    public List<Role> findByKeyInAndProjectAndDeletedFalse(List<String> roleKeys, Project project) {
        return roleRepository.findByKeyInAndProjectAndDeletedFalse(roleKeys, project);
    }

    private static String escapeLike(String input) {
        if (input == null) return null;
        return input.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private Project getCurrentProject() {
        return authorRequestScope.getProject();
    }

    private void deleteRoles(List<String> roleKeys, boolean cascade) {

        Project projectData = getCurrentProject();

        var roleEntities = findRolesAndThrowIfNotExists(roleKeys);

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        var userScopeRoleList =
                userScopeRoleRepository.findByRoleInAndDeletedFalseAndUserDeletedFalse(
                        roleEntities);

        var policyItemList = policyItemRepository.findByRoleInAndPolicyDeletedFalse(roleEntities);

        if (cascade) {
            if (!userScopeRoleList.isEmpty()) {
                log.debug("Soft-Deleting UserScopeRole");
                userScopeRoleList.forEach(userScopeRole -> userScopeRole.setDeleted(true));
                userScopeRoleRepository.saveAll(userScopeRoleList);
            }
            if (!policyItemList.isEmpty()) {
                log.debug("Soft-Deleting PolicyItem");
                policyService.deletePolicyItems(policyItemList);
            }
        } else {
            if (!userScopeRoleList.isEmpty() || !policyItemList.isEmpty()) {
                List<UserConflictWithRoleDto> userDtos =
                        userScopeRoleList.stream()
                                .map(
                                        userScopeRole -> {
                                            User user = userScopeRole.getUser();
                                            return UserConflictWithRoleDto.builder()
                                                    .key(user.getUserKey())
                                                    .name(user.getName())
                                                    .build();
                                        })
                                .toList();
                var policyKeySet =
                        policyItemList.stream()
                                .map(PolicyItem::getPolicy)
                                .map(Policy::getKey)
                                .collect(Collectors.toSet());

                var detail = roleMapper.toDetailedUsageByUsersPolicies(userDtos, policyKeySet);
                throw new RoleBeingUsedException(detail);
            }
        }

        softDeleteRoles(roleEntities, deletedDatetime, roleKeys, projectData);
    }

    private void softDeleteRoles(
            List<Role> roles, String deletedDatetime, List<String> roleNames, Project projectData) {

        auditService.commitAsync(roles);

        String memberKey = authorRequestScope.getMemberKey();
        for (var role : roles) {
            role.setKey(DELETED + "-" + deletedDatetime + "-" + role.getKey());
            role.setUpdatedBy(memberKey);
            role.setDeleted(true);
        }

        roleRepository.saveAll(roles);

        log.info(
                "Successfully Deleted Roles: {} from Project: {}", roleNames, projectData.getKey());
    }

    @Override
    public ListResponseDto<ScopeUserDto> listScopeUsersByRole(
            String roleKey, RoleScopeUserRequestDto dto) {

        Project project = getCurrentProject();
        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize());

        String scopeKey = StringUtils.isNotBlank(dto.getScopeKey()) ? dto.getScopeKey() : null;
        String userNameOrUserKeyILike =
                StringUtils.isNotBlank(dto.getUserNameOrUserKeyContains())
                        ? dto.getUserNameOrUserKeyContains()
                                .replace("\\", "\\\\")
                                .replace("%", "\\%")
                                .replace("_", "\\_")
                        : null;

        Page<ScopeUserDto> resultPage =
                userScopeRoleRepository
                        .findScopeUsersByRoleKey(
                                roleKey, project, scopeKey, userNameOrUserKeyILike, pageable)
                        .map(
                                p ->
                                        new ScopeUserDto(
                                                p.getUserKey(),
                                                p.getUserName(),
                                                p.getScopeKey(),
                                                p.getScopeName()));

        ProjectDto projectDto = projectMapper.toProjectDto(project);
        return ListResponseDto.create(resultPage, projectDto);
    }
}
