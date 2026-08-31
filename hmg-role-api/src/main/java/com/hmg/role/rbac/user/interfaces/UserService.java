package com.hmg.role.rbac.user.interfaces;

import com.hmg.role.rbac.user.dto.CreateUserDto;
import com.hmg.role.rbac.user.dto.DeleteBulkUserDto;
import com.hmg.role.rbac.user.dto.UpdateBulkUserDto;
import com.hmg.role.rbac.user.dto.UpdateUserDto;
import com.hmg.role.rbac.user.dto.UserDto;
import com.hmg.role.rbac.user.dto.UserSearchRequestDto;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.List;

public interface UserService {

    UserDto createUser(CreateUserDto createUserDto);

    ListResponseDto<UserDto> createBulkUsers(List<CreateUserDto> createUserDtos);

    ListResponseDto<UserDto> listUser(UserSearchRequestDto userSearchRequestDto);

    //    ListResponseDto<UserDto> listUserBySelectedRole(
    //            UserRequestByRoleKeyDto userRequestByRoleKeyDto);

    UserDto getUserByKey(String userKey);

    UserDto updateUser(String userKey, UpdateUserDto updateUserDto);

    ListResponseDto<UserDto> updateBulkUsers(List<UpdateBulkUserDto> updateBulkUserDtos);

    void deleteUser(String userKey);

    void deleteBulkUsers(DeleteBulkUserDto deleteBulkUserDtos);
}
