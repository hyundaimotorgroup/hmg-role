package com.hmg.role.abac.policy.interfaces;

import com.hmg.role.abac.permission.dto.AbacPolicySearchDto;
import com.hmg.role.abac.policy.dto.AbacPolicyDto;
import com.hmg.role.abac.policy.dto.DeleteBulkAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateBulkAbacPolicyDto;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.List;

public interface AbacPolicyService {

    AbacPolicyDto createPolicy(AbacPolicyDto abacPolicyDto);

    ListResponseDto<AbacPolicyDto> createBulkPolicies(List<AbacPolicyDto> abacPolicyDtos);

    ListResponseDto<AbacPolicyDto> getPolicies(AbacPolicySearchDto abacPolicySearchDto);

    AbacPolicyDto getPolicyByKey(String policyKey);

    AbacPolicyDto updatePolicy(String policyKey, UpdateAbacPolicyDto updateAbacPolicyDto);

    ListResponseDto<AbacPolicyDto> updateBulkPolicies(
            List<UpdateBulkAbacPolicyDto> updateBulkAbacPolicyDtos);

    void deletePolicy(String policyKey);

    void deleteBulkPolicies(DeleteBulkAbacPolicyDto deleteBulkAbacPolicyDto);
}
