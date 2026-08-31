package com.hmg.role.api.rbac;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.rbac.user.dto.CreateUserDto;
import com.hmg.role.rbac.user.dto.DeleteBulkUserDto;
import com.hmg.role.rbac.user.dto.UpdateBulkUserDto;
import com.hmg.role.rbac.user.dto.UpdateUserDto;
import com.hmg.role.rbac.user.dto.UserDto;
import com.hmg.role.rbac.user.dto.UserSearchRequestDto;
import com.hmg.role.rbac.user.interfaces.UserService;
import com.hmg.role.util.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "RBAC user role")
@RequiredArgsConstructor
@RequestMapping("/api/rbac/v1/users")
@RestController
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create new User")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid CreateUserDto createUserDto) {
        return userService.createUser(createUserDto);
    }

    // multipleFlag are used for calling API via swagger
    @Operation(summary = "Bulk Create New User")
    @PostMapping(params = "multiple=true")
    @ResponseStatus(HttpStatus.CREATED)
    public ListResponseDto<UserDto> createBulkUsers(
            @RequestParam(name = "multiple") Boolean multipleFlag,
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid CreateUserDto> createUserDto) {
        return userService.createBulkUsers(createUserDto);
    }

    @Operation(summary = "List users")
    @GetMapping
    public ListResponseDto<UserDto> listUsers(
            @ParameterObject @ModelAttribute @Valid UserSearchRequestDto userSearchRequestDto) {
        return userService.listUser(userSearchRequestDto);
    }

    //    TODO: no need to use this endpoint, just used listUsers but with filter Role
    //    @Operation(summary = "List User by Selected Role")
    //    @GetMapping("/list/selected-role")
    //    public ListResponseDto<UserDto> listUserBySelectedRole(
    //            @ParameterObject @ModelAttribute @Valid
    //                    UserRequestByRoleKeyDto userRequestByRoleKeyDto) {
    //        return userService.listUserBySelectedRole(userRequestByRoleKeyDto);
    //    }

    @Operation(summary = "Get user by user key")
    @GetMapping("/{key}")
    public UserDto getUserById(@PathVariable String key) {
        return userService.getUserByKey(key);
    }

    @Operation(summary = "Update existing user")
    @PutMapping("/{key}")
    public UserDto updateUser(
            @PathVariable String key, @RequestBody @Valid UpdateUserDto updateUserDto) {
        return userService.updateUser(key, updateUserDto);
    }

    // multipleFlag are used for calling API via swagger
    @Operation(summary = "Bulk Update Existing Users")
    @PutMapping(params = "multiple=true")
    public ListResponseDto<UserDto> updateBulkUsers(
            @RequestParam(name = "multiple") Boolean multipleFlag,
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull UpdateBulkUserDto> updateBulkUserDtos) {
        return userService.updateBulkUsers(updateBulkUserDtos);
    }

    @Operation(summary = "Delete existing user")
    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String key) {
        userService.deleteUser(key);
    }

    // multipleFlag are used for calling API via swagger
    @Operation(summary = "Bulk Delete Existing Users")
    @DeleteMapping(params = "multiple=true")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBulkUsers(
            @RequestParam(name = "multiple") Boolean multipleFlag,
            @RequestBody @Valid DeleteBulkUserDto deleteBulkUserDtos) {
        userService.deleteBulkUsers(deleteBulkUserDtos);
    }
}
