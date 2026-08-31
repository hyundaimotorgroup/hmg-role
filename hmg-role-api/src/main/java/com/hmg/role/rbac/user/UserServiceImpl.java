package com.hmg.role.rbac.user;

import static com.hmg.role.util.Constants.DELETED;
import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;

import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.ProjectMapper;
import com.hmg.role.admin.project.dto.ProjectDto;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.role.RoleRepository;
import com.hmg.role.rbac.role.exceptions.RoleNotFoundException;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.scope.ScopeMapper;
import com.hmg.role.rbac.scope.ScopeRepository;
import com.hmg.role.rbac.scope.exceptions.ScopeNotFoundException;
import com.hmg.role.rbac.user.dto.CreateUserDto;
import com.hmg.role.rbac.user.dto.DeleteBulkUserDto;
import com.hmg.role.rbac.user.dto.UpdateBulkUserDto;
import com.hmg.role.rbac.user.dto.UpdateUserDto;
import com.hmg.role.rbac.user.dto.UserDto;
import com.hmg.role.rbac.user.dto.UserSearchRequestDto;
import com.hmg.role.rbac.user.exceptions.UserAlreadyExistException;
import com.hmg.role.rbac.user.exceptions.UserNotFoundException;
import com.hmg.role.rbac.user.interfaces.UserService;
import com.hmg.role.rbac.userscoperole.UserScopeRole;
import com.hmg.role.rbac.userscoperole.UserScopeRoleMapper;
import com.hmg.role.rbac.userscoperole.UserScopeRoleRepository;
import com.hmg.role.rbac.userscoperole.dto.CreateUserScopeRoleDto;
import com.hmg.role.rbac.userscoperole.dto.UpdateUserScopeRoleDto;
import com.hmg.role.rbac.userscoperole.dto.UserScopeRoleDto;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.exceptions.BadRequestException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ScopeRepository scopeRepository;

    private final AuditService auditService;

    private final UserMapper userMapper;
    private final UserScopeRoleMapper userScopeRoleMapper;
    private final ProjectMapper projectMapper;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    private final UserScopeRoleRepository userScopeRoleRepository;
    private ScopeMapper scopeMapper;

    public UserDto createUser(CreateUserDto createUserDto) {
        return createBulkUsers(List.of(createUserDto)).results().getFirst();
    }

    public ListResponseDto<UserDto> createBulkUsers(List<CreateUserDto> createUserDtos) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        List<String> userKeys = createUserDtos.stream().map(CreateUserDto::key).toList();

        List<User> existingUsers =
                userRepository.findByUserKeyInAndProjectAndDeletedFalse(userKeys, projectData);
        List<String> existingUserKeys = existingUsers.stream().map(User::getUserKey).toList();

        if (!existingUserKeys.isEmpty()) {
            throw new UserAlreadyExistException(existingUserKeys);
        }

        List<String> allRoleKeys =
                createUserDtos.stream()
                        .filter(
                                createDto ->
                                        createDto.scopeRoles() != null
                                                && !createDto.scopeRoles().isEmpty())
                        .flatMap(dto -> dto.scopeRoles().stream())
                        .map(CreateUserScopeRoleDto::roleKey)
                        .toList();

        List<String> allScopeKeys =
                createUserDtos.stream()
                        .filter(
                                createDto ->
                                        createDto.scopeRoles() != null
                                                && !createDto.scopeRoles().isEmpty())
                        .flatMap(dto -> dto.scopeRoles().stream())
                        .map(CreateUserScopeRoleDto::scopeKey)
                        .toList();

        List<Role> allRoles =
                roleRepository.findRoleByKeyInAndProjectAndDeletedFalse(allRoleKeys, projectData);
        List<Scope> allScopes =
                scopeRepository.findByKeyInAndProjectAndDeletedFalse(allScopeKeys, projectData);

        List<String> existingRoleKeys = allRoles.stream().map(Role::getKey).toList();
        List<String> notFoundRoles =
                allRoleKeys.stream().filter(k -> !existingRoleKeys.contains(k)).toList();

        List<String> existingScopeKeys = allScopes.stream().map(Scope::getKey).toList();

        if (!notFoundRoles.isEmpty()) {
            throw new RoleNotFoundException(notFoundRoles);
        }

        List<String> missingScopes =
                allScopeKeys.stream().filter(k -> !existingScopeKeys.contains(k)).toList();
        if (!missingScopes.isEmpty()) throw new ScopeNotFoundException(missingScopes);

        Map<String, Role> roleMap =
                allRoles.stream().collect(Collectors.toMap(Role::getKey, Function.identity()));
        Map<String, Scope> scopeMap =
                allScopes.stream().collect(Collectors.toMap(Scope::getKey, Function.identity()));

        final Project projectDataFinal = projectData;

        String authorKey = authorRequestScope.getMemberKey();
        List<User> usersToSave =
                createUserDtos.stream()
                        .map(dto -> userMapper.toUser(dto, projectDataFinal))
                        .peek(
                                user -> {
                                    user.setCreatedBy(authorKey);
                                    user.setUpdatedBy(authorKey);
                                })
                        .toList();

        List<User> savedUsers = userRepository.saveAll(usersToSave);

        auditService.commitAsync(savedUsers);

        List<UserScopeRole> scopedRoles = new ArrayList<>();
        for (int i = 0; i < savedUsers.size(); i++) {
            User user = savedUsers.get(i);
            CreateUserDto dto = createUserDtos.get(i);

            List<UserScopeRole> roles =
                    userScopeRoleMapper.toCreateEntities(dto.scopeRoles(), user, roleMap, scopeMap);
            scopedRoles.addAll(roles);
        }

        userScopeRoleRepository.saveAll(scopedRoles);

        auditService.commitAsync(scopedRoles);

        List<UserDto> resultDtos =
                savedUsers.stream()
                        .map(
                                user -> {
                                    List<UserScopeRoleDto> userScopeRoleDtos =
                                            scopedRoles.stream()
                                                    .filter(
                                                            sr ->
                                                                    sr.getUser()
                                                                            .getUserKey()
                                                                            .equals(
                                                                                    user
                                                                                            .getUserKey()))
                                                    .map(userScopeRoleMapper::toDto)
                                                    .toList();
                                    return userMapper.toUserDto(user, userScopeRoleDtos);
                                })
                        .toList();

        log.info("Successfully Created Users: {} from Project: {}", userKeys, projectData.getKey());

        ProjectDto projectDto = getProjectDto();
        return ListResponseDto.create(
                new PageImpl<>(resultDtos, PageRequest.of(0, resultDtos.size()), resultDtos.size()),
                projectDto);
    }

    private Project getProject() {
        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }
        return projectData;
    }

    private ProjectDto getProjectDto() {
        Project project = getProject();
        return projectMapper.toProjectDto(project);
    }

    public ListResponseDto<UserDto> listUser(UserSearchRequestDto req) {

        var spec = new UserSpecificationBuilder().andProjectEqual(getProject());

        // if both no role and scope
        if (StringUtils.isBlank(req.getRole()) && StringUtils.isBlank(req.getScope())) {
            spec.fetchScopeRolesOrNoScopeRoles();
        } else {
            spec.fetchScopeRoles();
        }

        // filter role
        if (StringUtils.isNotBlank(req.getRole())) {
            spec.andRoleKeyEqual(req.getRole());
        }

        // filter scope
        if (StringUtils.isNotBlank(req.getScope())) {
            spec.andScopeKeyEqual(req.getScope());
        }

        if (StringUtils.isNotBlank(req.getType())) {
            if (StringUtils.isNotBlank(req.getKeyword())) {

                switch (req.getType().toUpperCase()) {
                    case "NAME" -> spec.andUserNameILike(req.getKeyword());
                    case "KEY" -> spec.andUserKeyILike(req.getKeyword());
                    case "NAME_KEY" -> spec.andUserKeyOrUserNameILike(req.getKeyword());
                    case "ROLE_NAME" -> spec.andRoleNameILike(req.getKeyword());
                    default -> throw new BadRequestException("Invalid dataType: " + req.getType());
                }

            } else {
                throw new BadRequestException("Keyword is required if dataType is not blank");
            }
        } else {
            if (StringUtils.isNotBlank(req.getKeyword())) {
                throw new BadRequestException("Type is required if keyword is not blank");
            } else {
                // dataType & keyword are both blank -> no filter
            }
        }

        // special spec to enable sorting based on specification
        Specification<User> effectiveSpec = spec.build().and(UserOrderingSpec.withBucketedOrder());
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize());

        var userPage = userRepository.findAll(effectiveSpec, pageable);
        List<User> pageUsers = userPage.getContent();

        Map<Long, List<UserScopeRole>> scopedRolesByUserId =
                pageUsers.isEmpty()
                        ? Map.of()
                        : userScopeRoleRepository.findActiveByUser(pageUsers).stream()
                                .collect(Collectors.groupingBy(sr -> sr.getUser().getId()));

        var userDtoPage =
                userPage.map(
                        user -> {
                            var userScopeRoleDtoList =
                                    scopedRolesByUserId
                                            .getOrDefault(user.getId(), List.of())
                                            .stream()
                                            .map(
                                                    userScopeRole -> {
                                                        var scopeDto =
                                                                scopeMapper.toScopeDto(
                                                                        userScopeRole.getScope());
                                                        return new UserScopeRoleDto(
                                                                userScopeRole.getRole().getKey(),
                                                                userScopeRole.getRole().getName(),
                                                                scopeDto.key(),
                                                                scopeDto.name());
                                                    })
                                            .distinct()
                                            .toList();
                            return userMapper.toUserDto(user, userScopeRoleDtoList);
                        });

        ProjectDto projectDto = getProjectDto();
        return ListResponseDto.create(userDtoPage, projectDto);
    }

    public UserDto getUserByKey(String userKey) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        var userEntity =
                userRepository
                        .findByUserKeyAndProjectAndDeletedFalse(userKey, projectData)
                        .orElseThrow(() -> new UserNotFoundException(userKey));

        List<UserScopeRoleDto> userScopeRoleDtos =
                userScopeRoleRepository.findActiveByUserWithRoleAndScope(userEntity).stream()
                        .map(userScopeRoleMapper::toDto)
                        .distinct()
                        .toList();

        return userMapper.toUserDto(userEntity, userScopeRoleDtos);
    }

    public UserDto updateUser(String userKey, UpdateUserDto updateUserDto) {

        UpdateBulkUserDto updateBulkUserDto =
                UpdateBulkUserDto.builder()
                        .key(userKey)
                        .name(updateUserDto.name())
                        .metadata(updateUserDto.metadata())
                        .scopeRoles(updateUserDto.scopeRoles())
                        .build();

        return updateBulkUsers(List.of(updateBulkUserDto)).results().getFirst();
    }

    // TODO: refactor this code to be more readable and efficient
    // Detect unchanges data logic
    public ListResponseDto<UserDto> updateBulkUsers(List<UpdateBulkUserDto> updateBulkUserDtos) {
        Project projectData = getProject();

        ProjectDto projectDto = getProjectDto();

        List<String> userKeys = updateBulkUserDtos.stream().map(UpdateBulkUserDto::key).toList();

        List<User> users =
                userRepository.findByUserKeyInAndProjectAndDeletedFalse(userKeys, projectData);
        Map<String, User> userMap =
                users.stream().collect(Collectors.toMap(User::getUserKey, Function.identity()));

        List<String> notFoundUsers =
                userKeys.stream().filter(k -> !userMap.containsKey(k)).toList();
        if (!notFoundUsers.isEmpty()) {
            throw new UserNotFoundException(notFoundUsers);
        }

        List<String> allRoleKeys =
                updateBulkUserDtos.stream()
                        .filter(
                                updateDto ->
                                        updateDto.scopeRoles() != null
                                                && !updateDto.scopeRoles().isEmpty())
                        .flatMap(dto -> dto.scopeRoles().stream())
                        .map(UpdateUserScopeRoleDto::roleKey)
                        .distinct()
                        .toList();
        List<String> allScopeKeys =
                updateBulkUserDtos.stream()
                        .filter(
                                updateDto ->
                                        updateDto.scopeRoles() != null
                                                && !updateDto.scopeRoles().isEmpty())
                        .flatMap(dto -> dto.scopeRoles().stream())
                        .map(UpdateUserScopeRoleDto::scopeKey)
                        .distinct()
                        .toList();

        List<Role> roles =
                roleRepository.findRoleByKeyInAndProjectAndDeletedFalse(allRoleKeys, projectData);
        List<Scope> scopes =
                scopeRepository.findByKeyInAndProjectAndDeletedFalse(allScopeKeys, projectData);

        List<UserScopeRole> allActiveScopeRoles = userScopeRoleRepository.findActiveByUser(users);

        Map<String, Role> roleMap =
                roles.stream().collect(Collectors.toMap(Role::getKey, Function.identity()));
        Map<String, Scope> scopeMap =
                scopes.stream().collect(Collectors.toMap(Scope::getKey, Function.identity()));

        List<String> missingRoles =
                allRoleKeys.stream().filter(k -> !roleMap.containsKey(k)).toList();
        if (!missingRoles.isEmpty()) throw new RoleNotFoundException(missingRoles);

        List<String> missingScopes =
                allScopeKeys.stream().filter(k -> !scopeMap.containsKey(k)).toList();
        if (!missingScopes.isEmpty()) throw new ScopeNotFoundException(missingScopes);

        String authorKey = authorRequestScope.getMemberKey();
        List<User> updatedUsers = new ArrayList<>();

        for (UpdateBulkUserDto dto : updateBulkUserDtos) {
            User user = userMap.get(dto.key());
            userMapper.toUser(user, dto);
            user.setUpdatedBy(authorKey);

            updatedUsers.add(user);
        }

        var savedUpdatedUsers = userRepository.saveAll(updatedUsers);
        auditService.commitAsync(savedUpdatedUsers);

        Map<String, User> updatedUserMap =
                savedUpdatedUsers.stream()
                        .collect(Collectors.toMap(User::getUserKey, Function.identity()));

        allActiveScopeRoles.forEach(sr -> sr.setDeleted(true));
        userScopeRoleRepository.saveAll(allActiveScopeRoles);
        auditService.commitAsync(allActiveScopeRoles);

        List<UserScopeRole> updatedScopeRoles = new ArrayList<>();
        for (UpdateBulkUserDto dto : updateBulkUserDtos) {
            User user = updatedUserMap.get(dto.key());

            if (dto.scopeRoles() == null || dto.scopeRoles().isEmpty()) {
                continue;
            }

            List<UserScopeRole> newScopeRoles =
                    userScopeRoleMapper.toUpdateEntities(dto.scopeRoles(), user, roleMap, scopeMap);

            updatedScopeRoles.addAll(newScopeRoles);
        }

        var savedUpdatedScopeRoles = userScopeRoleRepository.saveAll(updatedScopeRoles);
        auditService.commitAsync(savedUpdatedScopeRoles);

        if (savedUpdatedScopeRoles.isEmpty()) {
            List<UserDto> userDtos =
                    savedUpdatedUsers.stream()
                            .map(
                                    user -> {
                                        List<UserScopeRoleDto> userScopeRoleDtos =
                                                new ArrayList<>();
                                        return userMapper.toUserDto(user, userScopeRoleDtos);
                                    })
                            .toList();

            Page<UserDto> dtoPage =
                    new PageImpl<>(userDtos, PageRequest.of(0, userDtos.size()), userDtos.size());

            log.info(
                    "Successfully updated users: {} in project: {}",
                    userKeys,
                    projectData.getKey());

            return ListResponseDto.create(dtoPage, projectDto);
        }

        var savedUpdatedScopeRoleMap =
                savedUpdatedScopeRoles.stream()
                        .collect(Collectors.groupingBy(UserScopeRole::getUser));

        List<UserDto> userDtos =
                savedUpdatedUsers.stream()
                        .map(
                                user -> {
                                    if (savedUpdatedScopeRoleMap.containsKey(user)) {
                                        var currentScopedRoles = savedUpdatedScopeRoleMap.get(user);
                                        List<UserScopeRoleDto> userScopeRoleDtos =
                                                currentScopedRoles.stream()
                                                        .map(userScopeRoleMapper::toDto)
                                                        .toList();
                                        return userMapper.toUserDto(user, userScopeRoleDtos);
                                    }
                                    return null;
                                })
                        .filter(Objects::nonNull)
                        .toList();

        Page<UserDto> dtoPage =
                new PageImpl<>(userDtos, PageRequest.of(0, userDtos.size()), userDtos.size());

        log.info("Successfully updated users: {} in project: {}", userKeys, projectData.getKey());

        return ListResponseDto.create(dtoPage, projectDto);
    }

    public void deleteUser(String userKey) {

        var keys = List.of(userKey);
        DeleteBulkUserDto dto = DeleteBulkUserDto.builder().keys(keys).build();

        deleteBulkUsers(dto);
    }

    public void deleteBulkUsers(DeleteBulkUserDto deleteBulkUserDtos) {

        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        var userKeys = deleteBulkUserDtos.keys();

        var userEntities =
                userRepository.findByUserKeyInAndProjectAndDeletedFalse(userKeys, projectData);

        var existingUserKeys = userEntities.stream().map(User::getUserKey).toList();

        var nonExistentUserKeys =
                userKeys.stream().filter(userKey -> !existingUserKeys.contains(userKey)).toList();

        if (!nonExistentUserKeys.isEmpty()) {
            throw new UserNotFoundException(nonExistentUserKeys);
        }

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        var userDataList = userEntities.stream().toList();

        auditService.commitAsync(userDataList);

        userDataList.forEach(
                user -> {
                    user.setUserKey(DELETED + "-" + deletedDatetime + "-" + user.getUserKey());
                    user.setDeleted(true);
                });

        userRepository.saveAll(userDataList);
        log.info(
                "Successfully Deleted Users: {} from Project : {}", userKeys, projectData.getKey());
    }

    private User getByKey(String userKey) {
        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }

        return userRepository
                .findByUserKeyAndProjectAndDeletedFalse(userKey, projectData)
                .orElseThrow(() -> new UserNotFoundException(userKey));
    }

    @Autowired
    public void setScopeMapper(ScopeMapper scopeMapper) {
        this.scopeMapper = scopeMapper;
    }
}
