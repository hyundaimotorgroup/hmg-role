package com.hmg.role.api.abac;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.abac.userset.dto.DeleteBulkUserSetDto;
import com.hmg.role.abac.userset.dto.UpdateBulkUserSetDto;
import com.hmg.role.abac.userset.dto.UpdateUserSetDto;
import com.hmg.role.abac.userset.dto.UserSetDto;
import com.hmg.role.abac.userset.dto.UserSetSearchDto;
import com.hmg.role.abac.userset.interfaces.UserSetService;
import com.hmg.role.util.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UserSet")
@RequiredArgsConstructor
@RequestMapping("/api/abac/v1/user-sets")
@RestController
public class UserSetController {

    private final UserSetService userSetService;

    @Operation(summary = "Create New User Set")
    @PostMapping(params = "multiple=false")
    @Parameter(name = "multiple", schema = @Schema(allowableValues = {"true", "false"}))
    public UserSetDto create(@RequestBody @Valid UserSetDto userSetDto) {
        return userSetService.createUserSet(userSetDto);
    }

    // multiple is a flag variable are used only for swagger ui
    @Operation(summary = "Create Bulk New User Sets")
    @PostMapping(params = "multiple=true")
    @Parameter(
            name = "multiple",
            schema = @Schema(allowableValues = {"true", "false"}),
            required = true)
    public ListResponseDto<UserSetDto> createBulk(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull UserSetDto> userSetDtoList) {
        return userSetService.createBulkUserSets(userSetDtoList);
    }

    @Operation(summary = "List User Sets")
    @GetMapping
    public ListResponseDto<UserSetDto> list(
            @ParameterObject @ModelAttribute @Valid UserSetSearchDto userSetSearchDto) {
        return userSetService.getUserSets(userSetSearchDto);
    }

    @Operation(summary = "Get User Set by Key")
    @GetMapping("/{key}")
    public UserSetDto getByKey(@PathVariable String key) {
        return userSetService.getUserSetByKey(key);
    }

    @Operation(summary = "Update User Set by Key")
    @PutMapping("/{key}")
    public UserSetDto update(
            @PathVariable String key, @RequestBody @Valid UpdateUserSetDto userSetDto) {
        return userSetService.updateUserSet(key, userSetDto);
    }

    @Operation(summary = "Update Bulk Existing User Sets")
    @PutMapping
    public ListResponseDto<UserSetDto> updateBulk(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull UpdateBulkUserSetDto> userSetDto) {
        return userSetService.updateBulkUserSets(userSetDto);
    }

    @Operation(summary = "Delete User Set by Key")
    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "false") Boolean cascade) {

        if (Boolean.TRUE.equals(cascade)) {
            userSetService.deleteUserSetCascade(key);
        } else {
            userSetService.deleteUserSet(key);
        }
    }

    @Operation(summary = "Delete Bulk Existing User Sets")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBulk(
            @RequestBody @Valid DeleteBulkUserSetDto deleteBulkUserSetDto,
            @RequestParam(required = false, defaultValue = "false") Boolean cascade) {

        if (Boolean.TRUE.equals(cascade)) {
            userSetService.deleteBulkUserSetsCascade(deleteBulkUserSetDto);
        } else {
            userSetService.deleteBulkUserSets(deleteBulkUserSetDto);
        }
    }
}
