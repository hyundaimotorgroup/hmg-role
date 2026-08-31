package com.hmg.role.admin.member;

import static com.hmg.role.util.Constants.DELETED;
import static com.hmg.role.util.Constants.DELETED_DATE_FORMAT;

import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.admin.member.dto.CreateMemberDto;
import com.hmg.role.admin.member.dto.DeleteBulkMemberDto;
import com.hmg.role.admin.member.dto.MemberDto;
import com.hmg.role.admin.member.dto.UpdateBulkMemberDto;
import com.hmg.role.admin.member.dto.UpdateMemberDto;
import com.hmg.role.admin.member.exceptions.ApiKeyAlreadyExistException;
import com.hmg.role.admin.member.exceptions.MemberAlreadyExistException;
import com.hmg.role.admin.member.exceptions.MemberNotFoundException;
import com.hmg.role.admin.member.interfaces.MemberService;
import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.ProjectRepository;
import com.hmg.role.admin.project.exceptions.ProjectNotFoundException;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestDto;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final AuditService auditService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    @Override
    public MemberDto createMember(String projectKey, CreateMemberDto createMemberDto) {

        return createBulkMembers(projectKey, List.of(createMemberDto)).results().getFirst();
    }

    @Override
    public ListResponseDto<MemberDto> createBulkMembers(
            String projectKey, List<CreateMemberDto> createMemberDtoList) {

        var memberAdminData = authorRequestScope.getMemberKey();

        var project = getProjectByKeyOrThrowNotFound(projectKey);

        var memberKeys = createMemberDtoList.stream().map(CreateMemberDto::key).toList();

        validateMemberKeyAlreadyExist(project, memberKeys);

        validateApiKeyAlreadyExistForCreateMembers(createMemberDtoList);

        var mappedMember =
                createMemberDtoList.stream()
                        .map(dto -> memberMapper.toMember(dto, project, memberAdminData))
                        .toList();

        mappedMember.forEach(this::generateUUIDIfNotProvided);

        var createdMembers = memberRepository.saveAll(mappedMember);
        auditService.commitAsync(createdMembers);

        var results = createdMembers.stream().map(memberMapper::toMemberDto).toList();

        log.info("Successfully Created Members: {} for project: {}", memberKeys, projectKey);

        return ListResponseDto.create(results);
    }

    private Project getProjectByKeyOrThrowNotFound(String projectKey) {
        return projectRepository
                .findByKeyAndDeletedFalse(projectKey)
                .orElseThrow(() -> new ProjectNotFoundException(projectKey));
    }

    @Override
    public ListResponseDto<MemberDto> getAllMembers(
            String projectKey, PageRequestDto pageRequestDto) {

        var project = getProjectByKeyOrThrowNotFound(projectKey);

        var member =
                memberRepository
                        .findByProjectAndDeletedFalseOrderByUpdatedAtDesc(
                                project, pageRequestDto.pageRequest())
                        .map(memberMapper::toMemberDto);

        return ListResponseDto.create(member);
    }

    @Override
    public MemberDto getMemberByKey(String projectKey, String memberKey) {

        var member = getMemberByKeyOrThrowNotFound(projectKey, memberKey);

        return memberMapper.toMemberDto(member);
    }

    @Override
    public MemberDto updateMember(
            String projectKey, String memberKey, UpdateMemberDto updateMemberDto) {

        var memberAdminData = authorRequestScope.getMemberKey();

        var member = getMemberByKeyOrThrowNotFound(projectKey, memberKey);

        var apiKey = updateMemberDto.apiKey().toString();

        var existsByApiKey = memberRepository.existsByApiKeyAndDeletedFalse(apiKey);
        var requestApiKeyEquals = Objects.equals(apiKey, member.getApiKey());

        // check if the api key is going to be updated
        if (!requestApiKeyEquals && existsByApiKey) {
            // check if the new api key is unique
            throw new ApiKeyAlreadyExistException(apiKey);
        }

        memberMapper.toMember(member, updateMemberDto, memberAdminData);
        member = memberRepository.save(member);

        auditService.commitAsync(member);

        log.info("Successfully Updated Member: {}", memberKey);
        return memberMapper.toMemberDto(member);
    }

    @Override
    public ListResponseDto<MemberDto> updateBulkMembers(
            String projectKey, List<UpdateBulkMemberDto> updateBulkMemberDtoList) {

        var memberAdminData = authorRequestScope.getMemberKey();

        var project = getProjectByKeyOrThrowNotFound(projectKey);

        var memberKeys = updateBulkMemberDtoList.stream().map(UpdateBulkMemberDto::key).toList();

        var members = getAllMembersByKeysOrThrowNotFound(project, memberKeys);

        validateApiKeyAlreadyExistForUpdateMembers(updateBulkMemberDtoList);

        var memberMap =
                members.stream().collect(Collectors.toMap(Member::getKey, member -> member));

        List<Member> mappedMember =
                updateBulkMemberDtoList.stream()
                        .map(
                                dto ->
                                        mappingUpdateBulkMemberDtoToMember(
                                                dto, memberMap, memberAdminData))
                        .toList();

        var updatedMembers = memberRepository.saveAll(mappedMember);
        auditService.commitAsync(updatedMembers);

        var list = updatedMembers.stream().map(memberMapper::toMemberDto).toList();

        return ListResponseDto.create(list);
    }

    private Member mappingUpdateBulkMemberDtoToMember(
            UpdateBulkMemberDto dto, Map<String, Member> memberMap, String memberAdminKey) {
        var member = memberMap.get(dto.key());

        memberMapper.toMember(member, dto, memberAdminKey);

        return member;
    }

    @Override
    public void deleteMember(String projectKey, String memberKey) {

        var member = getMemberByKeyOrThrowNotFound(projectKey, memberKey);

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));
        member.setDeleted(true);
        member.setKey(DELETED + "-" + deletedDatetime + "-" + member.getKey());

        memberRepository.save(member);
        auditService.commitAsync(member);

        log.info("Successfully Deleted Member: {}", memberKey);
    }

    @Override
    public void deleteBulkMembers(String projectKey, DeleteBulkMemberDto deleteBulkMemberDto) {

        var project = getProjectByKeyOrThrowNotFound(projectKey);
        var members = getAllMembersByKeysOrThrowNotFound(project, deleteBulkMemberDto.keys());

        String deletedDatetime =
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DELETED_DATE_FORMAT));

        List<Member> updatedMembers = new ArrayList<>();

        members.forEach(
                member -> {
                    member.setDeleted(true);
                    member.setKey(DELETED + "-" + deletedDatetime + "-" + member.getKey());
                    updatedMembers.add(member);
                });

        memberRepository.saveAll(updatedMembers);
        auditService.commitAsync(updatedMembers);
    }

    private Member getMemberByKeyOrThrowNotFound(String projectKey, String memberKey) {

        var project = getProjectByKeyOrThrowNotFound(projectKey);

        return memberRepository
                .findByKeyAndProjectAndDeletedFalse(memberKey, project)
                .orElseThrow(() -> new MemberNotFoundException(memberKey));
    }

    private List<Member> getAllMembersByKeysOrThrowNotFound(
            Project project, List<String> inputMemberKeys) {

        var list = memberRepository.findByKeyInAndProjectAndDeletedFalse(inputMemberKeys, project);

        if (list.isEmpty()) {
            throw new MemberNotFoundException(inputMemberKeys);
        }

        var existingKeys = list.stream().map(Member::getKey).toList();

        var notFoundKeys =
                inputMemberKeys.stream().filter(key -> !existingKeys.contains(key)).toList();
        if (!notFoundKeys.isEmpty()) {
            throw new MemberNotFoundException(notFoundKeys);
        }

        return list;
    }

    private void validateMemberKeyAlreadyExist(Project project, List<String> memberKeys) {
        var foundList = memberRepository.findByKeyInAndProjectAndDeletedFalse(memberKeys, project);
        if (!foundList.isEmpty()) {
            var duplicateKeys = foundList.stream().map(Member::getKey).toList();
            throw new MemberAlreadyExistException(duplicateKeys);
        }
    }

    private void validateApiKeyAlreadyExistForCreateMembers(
            List<CreateMemberDto> createMemberDtoList) {

        var inputApiKeys =
                createMemberDtoList.stream()
                        .map(CreateMemberDto::apiKey)
                        .filter(Objects::nonNull)
                        .map(UUID::toString)
                        .toList();

        validateApiKeyAlreadyExist(inputApiKeys);
    }

    private void validateApiKeyAlreadyExistForUpdateMembers(
            List<UpdateBulkMemberDto> updateBulkMemberDtoList) {

        var apiKeysInput =
                updateBulkMemberDtoList.stream()
                        .map(UpdateBulkMemberDto::apiKey)
                        .filter(Objects::nonNull)
                        .map(UUID::toString)
                        .toList();

        validateApiKeyAlreadyExist(apiKeysInput);
    }

    private void validateApiKeyAlreadyExist(List<String> inputApiKeys) {

        var duplicateApiKeys =
                memberRepository.findByApiKeyInAndDeletedFalse(inputApiKeys).stream()
                        .map(Member::getApiKey)
                        .toList();

        if (!duplicateApiKeys.isEmpty()) {
            throw new ApiKeyAlreadyExistException(duplicateApiKeys);
        }
    }

    private void generateUUIDIfNotProvided(Member member) {
        if (member.getApiKey() == null) {
            UUID newUUID;

            // validate duplicate new uuid
            do {
                newUUID = UUID.randomUUID();
            } while (memberRepository.existsByApiKeyAndDeletedFalse(newUUID.toString()));

            member.setApiKey(newUUID);
        }
    }
}
