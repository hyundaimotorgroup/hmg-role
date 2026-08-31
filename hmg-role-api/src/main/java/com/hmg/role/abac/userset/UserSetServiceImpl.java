package com.hmg.role.abac.userset;

import static com.hmg.role.util.Constants.CASCADE_DISABLED;
import static com.hmg.role.util.Constants.CASCADE_ENABLED;
import static com.hmg.role.util.Constants.DELETED;
import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;
import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.abac.logicalexpression.dto.OperandDto;
import com.hmg.role.abac.policy.AbacPolicy;
import com.hmg.role.abac.policy.AbacPolicyItem;
import com.hmg.role.abac.policy.AbacPolicyRepository;
import com.hmg.role.abac.policy.policyitem.AbacPolicyItemRepository;
import com.hmg.role.abac.userset.attributes.AbacAttributeService;
import com.hmg.role.abac.userset.attributes.ConditionOperand;
import com.hmg.role.abac.userset.condition.UserSetCondition;
import com.hmg.role.abac.userset.condition.UserSetConditionOperandRepository;
import com.hmg.role.abac.userset.condition.UserSetConditionRepository;
import com.hmg.role.abac.userset.condition.UserSetOperand;
import com.hmg.role.abac.userset.dto.DeleteBulkUserSetDto;
import com.hmg.role.abac.userset.dto.UpdateBulkUserSetDto;
import com.hmg.role.abac.userset.dto.UpdateUserSetDto;
import com.hmg.role.abac.userset.dto.UserSetConflictDetailDto;
import com.hmg.role.abac.userset.dto.UserSetConflictWithPolicyDto;
import com.hmg.role.abac.userset.dto.UserSetDto;
import com.hmg.role.abac.userset.dto.UserSetSearchDto;
import com.hmg.role.abac.userset.exceptions.ParentUserSetNotFoundException;
import com.hmg.role.abac.userset.exceptions.UserSetAlreadyExistException;
import com.hmg.role.abac.userset.exceptions.UserSetIsBeingUsedException;
import com.hmg.role.abac.userset.exceptions.UserSetIsEmptyException;
import com.hmg.role.abac.userset.exceptions.UserSetNotFoundException;
import com.hmg.role.abac.userset.exceptions.UserSetTooManyException;
import com.hmg.role.abac.userset.interfaces.UserSetService;
import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.ProjectMapper;
import com.hmg.role.admin.project.dto.ProjectDto;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.enums.OperandPosition;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class UserSetServiceImpl implements UserSetService {

    private final UserSetRepository userSetRepository;
    private final UserSetConditionRepository userSetConditionRepository;
    private final UserSetConditionOperandRepository userSetConditionOperandRepository;
    private final AbacPolicyItemRepository abacPolicyItemRepository;
    private final AbacPolicyRepository abacPolicyRepository;

    private final AbacAttributeService attributeService;

    private final UserSetMapper userSetMapper;
    private final ProjectMapper projectMapper;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    public UserSetDto createUserSet(UserSetDto userSetDto) {

        var userSetDtoList = List.of(userSetDto);

        return createBulkProcess(userSetDtoList).getFirst();
    }

    public ListResponseDto<UserSetDto> createBulkUserSets(List<UserSetDto> userSetDtoList) {

        var createdUserSetResult = createBulkProcess(userSetDtoList);

        return ListResponseDto.create(createdUserSetResult);
    }

    private List<UserSetDto> createBulkProcess(List<UserSetDto> userSetDtos) {
        var projectData = getProject();

        validateCreateBulkUserSet(userSetDtos, projectData);
        validateExistingCreateBulkUserSet(userSetDtos, projectData);

        final Project projectDataFinal = projectData;

        var createdUserSetDtoList =
                userSetDtos.stream()
                        .map(userSetDto -> mapAndSaveUserSet(userSetDto, projectDataFinal))
                        .toList();

        var createdUserSetKeyList = createdUserSetDtoList.stream().map(UserSetDto::key).toList();

        log.info(
                "Successfully Created User Sets: {} from Project : {}",
                createdUserSetKeyList,
                projectData.getKey());

        return createdUserSetDtoList;
    }

    public ListResponseDto<UserSetDto> getUserSets(UserSetSearchDto dto) {

        Project projectData = getProject();

        String keyLike = escapeLike(dto.getKeyLike());
        String nameLike = escapeLike(dto.getNameLike());

        Page<UserSet> userSetPages =
                userSetRepository.findBySearchParameterAndDeletedFalse(
                        keyLike, nameLike, projectData, dto.pageRequest());

        List<UserSetDto> userSetDtos = new ArrayList<>();

        for (var userSetPage : userSetPages) {

            List<UserSetCondition> userSetConditionSetEntities =
                    userSetConditionRepository.findByUserSet(userSetPage);

            List<ConditionDto> conditionDtos = fetchUserConditions(userSetConditionSetEntities);

            UserSetDto userSetDto = userSetMapper.toUserSetDto(userSetPage, conditionDtos);

            userSetDtos.add(userSetDto);
        }

        var userSetDtoPages =
                new PageImpl<>(userSetDtos, dto.pageRequest(), userSetPages.getTotalElements());

        var projectDto = getProjectDto();
        return ListResponseDto.create(userSetDtoPages, projectDto);
    }

    public UserSetDto getUserSetByKey(String userSetKey) {

        Project projectData = getProject();

        UserSet userSetEntity =
                userSetRepository
                        .findByKeyAndProjectAndDeletedFalse(userSetKey, projectData)
                        .orElseThrow(UserSetNotFoundException::new);

        List<UserSetCondition> userSetConditionSetEntities =
                userSetConditionRepository.findByUserSet(userSetEntity);

        List<ConditionDto> conditionDtoList = fetchUserConditions(userSetConditionSetEntities);

        return userSetMapper.toUserSetDto(userSetEntity, conditionDtoList);
    }

    public UserSetDto updateUserSet(String userSetKey, UpdateUserSetDto userSetDto) {

        Project projectData = getProject();

        UserSet userSetEntity =
                userSetRepository
                        .findByKeyAndProjectAndDeletedFalse(userSetKey, projectData)
                        .orElseThrow(UserSetNotFoundException::new);

        /*Delete the existing data and update the new data*/
        deleteExistingUserSetCondition(userSetEntity);

        userSetMapper.toUserSet(userSetEntity, userSetDto);
        userSetEntity.setUpdatedBy(authorRequestScope.getMemberKey());
        saveUserSet(userSetEntity, userSetDto.conditionGroup(), projectData);

        log.info(
                "Successfully Updated user set: {} from Project: {}",
                userSetEntity.getKey(),
                projectData.getKey());

        var resultUserSetDto = userSetMapper.toUserSetDto(userSetKey, userSetDto);

        if (userSetEntity.getParents() != null) {

            List<String> parents =
                    userSetEntity.getParents().stream().map(UserSet::getKey).toList();

            resultUserSetDto = userSetMapper.toUserSetDto(userSetKey, parents, userSetDto);
            return resultUserSetDto;
        }

        return resultUserSetDto;
    }

    public ListResponseDto<UserSetDto> updateBulkUserSets(
            List<UpdateBulkUserSetDto> updateBulkUserSetDtoList) {

        Project projectData = getProject();

        var requestedUserSetKeyList =
                updateBulkUserSetDtoList.stream().map(UpdateBulkUserSetDto::key).toList();

        var userSets =
                userSetRepository.findByKeyInAndProjectAndDeletedFalse(
                        requestedUserSetKeyList, projectData);

        var existingUserSetKeyList = userSets.stream().map(UserSet::getKey).toList();

        validateUpdateBulkUserSet(requestedUserSetKeyList, existingUserSetKeyList);

        var mappedUserSets =
                userSets.stream()
                        .distinct()
                        .collect(Collectors.toMap(UserSet::getKey, userSet -> userSet));

        List<UserSetDto> userSetDtoList =
                updateBulkUserSetProcess(updateBulkUserSetDtoList, mappedUserSets, projectData);

        log.info(
                "Successfully Updated User Sets: {} from Project: {}",
                existingUserSetKeyList,
                projectData.getKey());

        var projectDto = getProjectDto();
        return ListResponseDto.create(userSetDtoList);
    }

    public void deleteUserSet(String userSetKey) {
        deleteBulkUserSets(List.of(userSetKey), CASCADE_DISABLED);
    }

    public void deleteUserSetCascade(String userSetKey) {
        deleteBulkUserSets(List.of(userSetKey), CASCADE_ENABLED);
    }

    public void deleteBulkUserSets(DeleteBulkUserSetDto deleteBulkUserSetDto) {

        var requestedUserSetKeys = deleteBulkUserSetDto.keys();

        deleteBulkUserSets(requestedUserSetKeys, CASCADE_DISABLED);
    }

    public void deleteBulkUserSetsCascade(DeleteBulkUserSetDto deleteBulkUserSetDto) {

        var requestedUserSetKeys = deleteBulkUserSetDto.keys();

        deleteBulkUserSets(requestedUserSetKeys, CASCADE_ENABLED);
    }

    private void deleteBulkUserSets(List<String> requestedUserSetKeys, boolean cascade) {

        Project projectData = getProject();

        var userSetEntities =
                userSetRepository.findByKeyInAndProjectAndDeletedFalse(
                        requestedUserSetKeys, projectData);

        var existingUserSetKeys = userSetEntities.stream().map(UserSet::getKey).toList();

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        // validation and delete process

        validateUpdateBulkUserSet(requestedUserSetKeys, existingUserSetKeys);

        validateAndUpdatePolicyItems(userSetEntities, deletedDatetime, cascade);

        validateAndUpdateUserSetHasChildren(userSetEntities);

        softDeleteUserSets(existingUserSetKeys, userSetEntities, deletedDatetime);
    }

    private void softDeleteUserSets(
            List<String> existingUserSetKeys,
            List<UserSet> userSetEntities,
            String deletedDatetime) {

        Project projectData = getProject();

        for (var userSet : userSetEntities) {
            userSet.setDeleted(true);
            userSet.setKey(DELETED + "-" + deletedDatetime + "-" + userSet.getKey());
        }

        userSetRepository.saveAll(userSetEntities);

        log.info(
                "Successfully Deleted User Sets: {} from Project: {}",
                existingUserSetKeys,
                projectData.getKey());
    }

    private void validateAndUpdatePolicyItems(
            List<UserSet> userSetEntities, String deletedDatetime, boolean cascade) {
        List<AbacPolicyItem> policyItems =
                abacPolicyItemRepository.findByUserSetInAndPolicy_DeletedFalse(userSetEntities);

        if (cascade && !policyItems.isEmpty()) {
            updatePoliciesAndPolicyItems(userSetEntities, policyItems, deletedDatetime);
        } else if (!policyItems.isEmpty()) {
            List<UserSetConflictWithPolicyDto> userSetDtos =
                    policyItems.stream()
                            .map(
                                    abacPolicyItem -> {
                                        UserSet userSet = abacPolicyItem.getUserSet();
                                        return UserSetConflictWithPolicyDto.builder()
                                                .userKey(userSet.getKey())
                                                .userSetName(userSet.getName())
                                                .build();
                                    })
                            .distinct()
                            .toList();
            Set<String> policyKeys =
                    policyItems.stream()
                            .map(pi -> pi.getPolicy().getKey())
                            .collect(Collectors.toSet());
            throw new UserSetIsBeingUsedException(
                    new UserSetConflictDetailDto(userSetDtos, policyKeys));
        }
    }

    private void updatePoliciesAndPolicyItems(
            List<UserSet> userSetEntities,
            List<AbacPolicyItem> policyItems,
            String deletedDatetime) {

        // Checking the Policies and Policy Items
        List<AbacPolicy> policies =
                policyItems.stream().map(AbacPolicyItem::getPolicy).distinct().toList();

        List<AbacPolicyItem> abacPolicyItems =
                abacPolicyItemRepository.findByPolicyInAndDeletedFalse(policies);

        var policyItemsWithoutDeletedUserSet =
                abacPolicyItems.stream()
                        .filter(
                                abacPolicyItem ->
                                        !userSetEntities.contains(abacPolicyItem.getUserSet()))
                        .toList();

        if (!policyItemsWithoutDeletedUserSet.isEmpty()) {
            softDeletedPolicyItems(abacPolicyItems, userSetEntities);
        } else {
            softDeletePolicies(policies, userSetEntities, deletedDatetime);
        }
    }

    private void softDeletedPolicyItems(
            List<AbacPolicyItem> abacPolicyItems, List<UserSet> userSetEntities) {

        var policyItemsWithDeletedUserSet =
                abacPolicyItems.stream()
                        .filter(
                                abacPolicyItem ->
                                        userSetEntities.contains(abacPolicyItem.getUserSet()))
                        .toList();

        var policySet = new HashSet<String>();
        var userSetKeySet = new HashSet<String>();
        for (var policyItem : policyItemsWithDeletedUserSet) {

            policyItem.setDeleted(true);

            policySet.add(policyItem.getPolicy().getKey());
            userSetKeySet.add(policyItem.getUserSet().getKey());
        }

        abacPolicyItemRepository.saveAll(policyItemsWithDeletedUserSet);

        log.info("Soft deleted policy items : {}, given user sets : {}", policySet, userSetKeySet);
    }

    private void softDeletePolicies(
            List<AbacPolicy> policies, List<UserSet> userSets, String deletedDatetime) {

        for (var policy : policies) {
            policy.setKey(DELETED + "-" + deletedDatetime + "-" + policy.getKey());
            policy.setDeleted(true);
        }

        abacPolicyRepository.saveAll(policies);

        log.info("Soft deleted policies : {}, given user sets : {}", policies, userSets);
    }

    private void validateAndUpdateUserSetHasChildren(List<UserSet> userSetEntities) {

        Project projectData = getProject();

        List<UserSet> userSetChildren =
                userSetRepository.findByParentsInAndProjectAndDeletedFalse(
                        userSetEntities, projectData);

        if (!userSetChildren.isEmpty()) {
            orphanUserSetChildren(userSetChildren);
        }
    }

    private void orphanUserSetChildren(List<UserSet> userSets) {
        Set<String> childKeys = new HashSet<>();
        for (var userSet : userSets) {
            childKeys.add(userSet.getKey());
            userSet.getParents().clear();
        }
        userSetRepository.saveAll(userSets);
        log.info("Orphaned user set children: {}", childKeys);
    }

    private void validateCreateBulkUserSet(List<UserSetDto> userSetDtos, Project projectData) {
        if (ObjectUtils.isEmpty(userSetDtos)) {
            throw new UserSetIsEmptyException();
        }

        var userSetKeyList = userSetDtos.stream().map(UserSetDto::key).toList();

        var existingUserSetEntities =
                userSetRepository.findByKeyInAndProjectAndDeletedFalse(userSetKeyList, projectData);

        var existingUserSetKeyList =
                existingUserSetEntities.stream()
                        .map(UserSet::getKey)
                        .filter(userSetKeyList::contains)
                        .toList();

        if (!existingUserSetKeyList.isEmpty()) {
            throw new UserSetAlreadyExistException(existingUserSetKeyList);
        }
    }

    private void validateExistingCreateBulkUserSet(List<UserSetDto> userSetDtos, Project project) {
        int newUserSetCount = userSetDtos.size();
        int existingUserSetCount = userSetRepository.countByProjectAndDeletedFalse(project);
        int totalUserSetCount = newUserSetCount + existingUserSetCount;
        if (totalUserSetCount > MAX_LIST_SIZE) {
            throw new UserSetTooManyException(existingUserSetCount, newUserSetCount, MAX_LIST_SIZE);
        }
    }

    private UserSetDto mapAndSaveUserSet(UserSetDto userSetDto, Project projectData) {
        List<UserSet> parentUserSets = null;

        /* since the parents are not required to input, we need to check that the parent are inputted or not.
          if inputted, then need to validate that is it available in database or not.
          if not inputted, then skip the parent validation process.
        */
        if (userSetDto.parents() != null && !userSetDto.parents().isEmpty()) {
            parentUserSets =
                    userSetRepository.findByKeyInAndProjectAndDeletedFalse(
                            userSetDto.parents(), projectData);

            validateParents(parentUserSets, userSetDto.parents());
        }

        var userSet = userSetMapper.toUserSet(userSetDto, projectData);

        String authorKey = authorRequestScope.getMemberKey();
        userSet.setCreatedBy(authorKey);
        userSet.setUpdatedBy(authorKey);

        if (parentUserSets != null) {
            userSet.setParents(parentUserSets);
        }
        saveUserSet(userSet, userSetDto.conditionGroup(), projectData);

        return userSetDto;
    }

    private void validateParents(List<UserSet> parentUserSets, List<String> parentKeys) {
        if (parentUserSets.isEmpty()) {
            throw new ParentUserSetNotFoundException(parentKeys);
        } else if (parentKeys.size() != parentUserSets.size()) {
            var foundParent = parentUserSets.stream().map(UserSet::getKey).toList();
            var unknownParent =
                    parentKeys.stream().filter(parent -> !foundParent.contains(parent)).toList();

            throw new ParentUserSetNotFoundException(unknownParent);
        }
    }

    private void validateUpdateBulkUserSet(
            List<String> requestedUserSetKeyList, List<String> existingUserSetKeyList) {

        var nonExistingUserSetKeyList =
                requestedUserSetKeyList.stream()
                        .filter(key -> !existingUserSetKeyList.contains(key))
                        .toList();

        if (!nonExistingUserSetKeyList.isEmpty())
            throw new UserSetNotFoundException(nonExistingUserSetKeyList);
    }

    private List<ConditionDto> fetchUserConditions(List<UserSetCondition> userSetConditionSetList) {

        List<ConditionDto> conditionDtoList = new ArrayList<>();

        for (var userConditionSet : userSetConditionSetList) {

            List<UserSetOperand> userSetOperandList =
                    userSetConditionOperandRepository.findByUserSetCondition(userConditionSet);

            OperandDto operandLeft = null;
            OperandDto operandRight = null;

            for (var userConditionOperand : userSetOperandList) {

                if (userConditionOperand.getPosition() == OperandPosition.LEFT) {
                    operandLeft =
                            OperandDto.builder()
                                    .operand(userConditionOperand.getOperand())
                                    .dataType(userConditionOperand.getDataType())
                                    .type(userConditionOperand.getType())
                                    .build();
                } else if (userConditionOperand.getPosition() == OperandPosition.RIGHT) {
                    operandRight =
                            OperandDto.builder()
                                    .operand(userConditionOperand.getOperand())
                                    .dataType(userConditionOperand.getDataType())
                                    .type(userConditionOperand.getType())
                                    .build();
                }
            }

            ConditionDto conditionDto =
                    ConditionDto.builder()
                            .left(operandLeft)
                            .operator(userConditionSet.getOperator())
                            .right(operandRight)
                            .build();
            conditionDtoList.add(conditionDto);
        }

        return conditionDtoList;
    }

    private UserSet saveUserSet(
            UserSet userSetEntity, List<ConditionDto> conditionSetDtoList, Project project) {
        userSetEntity = userSetRepository.save(userSetEntity);
        saveConditionGroups(userSetEntity, conditionSetDtoList, project);
        return userSetEntity;
    }

    private List<UserSetCondition> saveConditionGroups(
            UserSet userSetEntity, List<ConditionDto> conditionSetDtoList, Project project) {
        List<UserSetCondition> res = new LinkedList<>();
        for (var cond : conditionSetDtoList) {
            UserSetCondition userSetCondition = new UserSetCondition();
            userSetCondition.setUserSet(userSetEntity);
            userSetCondition.setOperator(cond.operator());

            // Save the UserCondition first so it gets an ID
            userSetCondition = userSetConditionRepository.save(userSetCondition);

            UserSetOperand left =
                    getOrCreateOperand(
                            userSetCondition, cond.left(), OperandPosition.LEFT, project);
            UserSetOperand right =
                    getOrCreateOperand(
                            userSetCondition, cond.right(), OperandPosition.RIGHT, project);

            List<UserSetOperand> userSetOperandEntities = List.of(left, right);

            userSetOperandEntities =
                    userSetConditionOperandRepository.saveAll(userSetOperandEntities);

            userSetCondition.setOperands(userSetOperandEntities);
            res.add(userSetCondition);
        }
        return res;
    }

    private UserSetOperand getOrCreateOperand(
            UserSetCondition userSetCondition,
            OperandDto operandDto,
            OperandPosition operandPosition,
            Project project) {
        ConditionOperand conditionOperand =
                attributeService.getOrCreateOperand(
                        OperandSubject.USER_SET,
                        operandDto.operand(),
                        operandDto.type(),
                        operandDto.dataType(),
                        project);

        // also would be too brittle to implement with mapper
        UserSetOperand userSetOperand = new UserSetOperand();
        userSetOperand.setUserSetCondition(userSetCondition);
        userSetOperand.setPosition(operandPosition);
        userSetOperand.setConditionOperand(conditionOperand);

        return userSetOperand;
    }

    private List<UserSetDto> updateBulkUserSetProcess(
            List<UpdateBulkUserSetDto> updateBulkUserSetDtoList,
            Map<String, UserSet> mappedUserSets,
            Project projectData) {

        List<UserSetDto> userSetDtoList = new ArrayList<>();

        // Delete existing conditions for all user sets in bulk
        deleteUserSetConditions(mappedUserSets.values(), projectData);

        List<UserSet> updatedUserSets = new ArrayList<>();

        for (var updateBulkUserSetDto : updateBulkUserSetDtoList) {

            var userSetEntity = mappedUserSets.get(updateBulkUserSetDto.key());
            userSetMapper.toUserSet(userSetEntity, updateBulkUserSetDto);
            userSetEntity.setUpdatedBy(authorRequestScope.getMemberKey());

            saveConditionGroups(userSetEntity, updateBulkUserSetDto.conditionGroup(), projectData);
            updatedUserSets.add(userSetEntity);

            var userSetDto =
                    userSetMapper.toUserSetDto(updateBulkUserSetDto, userSetEntity.getParents());

            userSetDtoList.add(userSetDto);
        }

        userSetRepository.saveAll(updatedUserSets);

        return userSetDtoList;
    }

    private void deleteExistingUserSetCondition(UserSet userSetEntity) {
        Project projectData = getProject();
        deleteUserSetConditions(List.of(userSetEntity), projectData);
    }

    private void deleteUserSetConditions(Collection<UserSet> userSetEntities, Project projectData) {
        List<UserSetCondition> userSetConditionEntities = new ArrayList<>();
        for (var userSetEntity : userSetEntities) {
            userSetConditionEntities.addAll(userSetEntity.getConditions());
        }

        List<UserSetOperand> userOperands = new ArrayList<>();
        for (var userConditionEntity : userSetConditionEntities) {
            userOperands.addAll(userConditionEntity.getOperands());
        }

        // Extract condition operands for opportunistic deletion of literals
        List<ConditionOperand> conditionOperands =
                userOperands.stream().map(UserSetOperand::getConditionOperand).toList();

        userSetConditionOperandRepository.deleteAll(userOperands);
        userSetConditionRepository.deleteAll(userSetConditionEntities);

        // Clear stale references to deleted conditions to prevent EntityNotFoundException
        for (var userSetEntity : userSetEntities) {
            userSetEntity.getConditions().clear();
        }

        // Opportunistically delete literals that are no longer in use
        attributeService.opportunisticDeleteLiterals(
                conditionOperands, OperandSubject.USER_SET, projectData);
    }

    private static String escapeLike(String input) {
        if (input == null) return null;
        return input.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private Project getProject() {
        Project projectData = authorRequestScope.getProject();
        if (projectData == null) {
            projectData = authorRequestScope.getMember().getProject();
        }
        return projectData;
    }

    private ProjectDto getProjectDto() {
        var project = getProject();
        return projectMapper.toProjectDto(project);
    }
}
