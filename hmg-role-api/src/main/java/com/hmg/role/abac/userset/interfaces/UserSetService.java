package com.hmg.role.abac.userset.interfaces;

import com.hmg.role.abac.userset.dto.DeleteBulkUserSetDto;
import com.hmg.role.abac.userset.dto.UpdateBulkUserSetDto;
import com.hmg.role.abac.userset.dto.UpdateUserSetDto;
import com.hmg.role.abac.userset.dto.UserSetDto;
import com.hmg.role.abac.userset.dto.UserSetSearchDto;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.List;

public interface UserSetService {

    UserSetDto createUserSet(UserSetDto userSetDto);

    ListResponseDto<UserSetDto> createBulkUserSets(List<UserSetDto> userSetDtos);

    ListResponseDto<UserSetDto> getUserSets(UserSetSearchDto dto);

    UserSetDto getUserSetByKey(String key);

    UserSetDto updateUserSet(String key, UpdateUserSetDto userSetDto);

    ListResponseDto<UserSetDto> updateBulkUserSets(
            List<UpdateBulkUserSetDto> updateBulkUserSetDtos);

    void deleteUserSet(String key);

    void deleteUserSetCascade(String key);

    void deleteBulkUserSets(DeleteBulkUserSetDto deleteBulkUserSetDto);

    void deleteBulkUserSetsCascade(DeleteBulkUserSetDto deleteBulkUserSetDto);
}
