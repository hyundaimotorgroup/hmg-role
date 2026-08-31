package com.hmg.role.api.admin;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.admin.member.dto.CreateMemberDto;
import com.hmg.role.admin.member.dto.DeleteBulkMemberDto;
import com.hmg.role.admin.member.dto.MemberDto;
import com.hmg.role.admin.member.dto.UpdateBulkMemberDto;
import com.hmg.role.admin.member.dto.UpdateMemberDto;
import com.hmg.role.admin.member.interfaces.MemberService;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member")
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/projects/{projectKey}/members")
@RestController
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "Create New Member")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public MemberDto createNewMember(
            @PathVariable String projectKey, @RequestBody @Valid CreateMemberDto createMemberDto) {

        return memberService.createMember(projectKey, createMemberDto);
    }

    @Operation(summary = "Bulk Create New Members")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(params = "multiple=true")
    public ListResponseDto<MemberDto> createBulkNewMembers(
            @Parameter(name = "multiple", required = true) Boolean multiple, // for swagger ui
            @PathVariable String projectKey,
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull CreateMemberDto> createMemberDtoList) {

        return memberService.createBulkMembers(projectKey, createMemberDtoList);
    }

    @Operation(summary = "List Members")
    @GetMapping
    public ListResponseDto<MemberDto> listMembers(
            @PathVariable String projectKey,
            @ParameterObject @ModelAttribute @Valid PageRequestDto pageRequestDto) {
        return memberService.getAllMembers(projectKey, pageRequestDto);
    }

    @Operation(summary = "Get Member by Key")
    @GetMapping("/{memberKey}")
    public MemberDto getMemberByKey(
            @PathVariable String projectKey, @PathVariable String memberKey) {
        return memberService.getMemberByKey(projectKey, memberKey);
    }

    @Operation(summary = "Update Existing Member")
    @PutMapping("/{memberKey}")
    public MemberDto updateMember(
            @PathVariable String projectKey,
            @PathVariable String memberKey,
            @RequestBody @Valid UpdateMemberDto updateMemberDto) {
        return memberService.updateMember(projectKey, memberKey, updateMemberDto);
    }

    @Operation(summary = "Bulk Update Existing Members")
    @PutMapping(params = "multiple=true")
    public ListResponseDto<MemberDto> updateMember(
            @Parameter(name = "multiple", required = true) Boolean multiple,
            @PathVariable String projectKey,
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull UpdateBulkMemberDto> updateBulkMemberDtoList) {
        return memberService.updateBulkMembers(projectKey, updateBulkMemberDtoList);
    }

    @Operation(summary = "Delete Existing Member")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{memberKey}")
    public void deleteBulkMembers(@PathVariable String projectKey, @PathVariable String memberKey) {
        memberService.deleteMember(projectKey, memberKey);
    }

    @Operation(summary = "Bulk Delete Existing Members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(params = "multiple=true")
    public void deleteBulkMembers(
            @Parameter(name = "multiple", required = true) Boolean multiple,
            @PathVariable String projectKey,
            @RequestBody @Valid DeleteBulkMemberDto deleteBulkMemberDto) {
        memberService.deleteBulkMembers(projectKey, deleteBulkMemberDto);
    }
}
