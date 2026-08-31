package com.hmg.role.admin.member;

import com.hmg.role.admin.member.dto.CreateMemberDto;
import com.hmg.role.admin.member.dto.MemberDto;
import com.hmg.role.admin.member.dto.UpdateBulkMemberDto;
import com.hmg.role.admin.member.dto.UpdateMemberDto;
import com.hmg.role.admin.project.Project;
import com.hmg.role.common.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public abstract class MemberMapper {

    public Member toMember(CreateMemberDto dto, Project project, String memberAdminKey) {
        var member = toMember(dto);
        member.setProject(project);
        member.setCreatedBy(memberAdminKey);
        member.setUpdatedBy(memberAdminKey);
        return member;
    }

    @Mapping(source = "dto.description", target = "description")
    public abstract Member toMember(CreateMemberDto dto);

    @Mapping(source = "memberAdminKey", target = "updatedBy")
    @Mapping(source = "dto.description", target = "description")
    public abstract void toMember(
            @MappingTarget Member member, UpdateMemberDto dto, String memberAdminKey);

    @Mapping(source = "memberAdminKey", target = "updatedBy")
    @Mapping(source = "dto.description", target = "description")
    public abstract void toMember(
            @MappingTarget Member member, UpdateBulkMemberDto dto, String memberAdminKey);

    @Mapping(source = "member.description", target = "description")
    public abstract MemberDto toMemberDto(Member member);
}
