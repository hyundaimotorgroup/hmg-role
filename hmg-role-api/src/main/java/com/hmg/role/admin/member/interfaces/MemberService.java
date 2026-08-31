package com.hmg.role.admin.member.interfaces;

import com.hmg.role.admin.member.dto.CreateMemberDto;
import com.hmg.role.admin.member.dto.DeleteBulkMemberDto;
import com.hmg.role.admin.member.dto.MemberDto;
import com.hmg.role.admin.member.dto.UpdateBulkMemberDto;
import com.hmg.role.admin.member.dto.UpdateMemberDto;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestDto;
import java.util.List;

public interface MemberService {
    MemberDto createMember(String projectKey, CreateMemberDto createMemberDto);

    ListResponseDto<MemberDto> createBulkMembers(
            String projectKey, List<CreateMemberDto> createMemberDtos);

    ListResponseDto<MemberDto> getAllMembers(String projectKey, PageRequestDto pageRequestDto);

    MemberDto getMemberByKey(String projectKey, String memberKey);

    MemberDto updateMember(String projectKey, String memberKey, UpdateMemberDto updateMemberDto);

    ListResponseDto<MemberDto> updateBulkMembers(
            String projectKey, List<UpdateBulkMemberDto> updateBulkMemberDtos);

    void deleteMember(String projectKey, String memberKey);

    void deleteBulkMembers(String projectKey, DeleteBulkMemberDto deleteBulkMemberDto);
}
