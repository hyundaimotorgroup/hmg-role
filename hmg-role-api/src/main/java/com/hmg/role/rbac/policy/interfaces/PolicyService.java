package com.hmg.role.rbac.policy.interfaces;

import com.hmg.role.rbac.policy.dto.CreatePolicyDto;
import com.hmg.role.rbac.policy.dto.DeleteBulkPolicyDto;
import com.hmg.role.rbac.policy.dto.PolicyDto;
import com.hmg.role.rbac.policy.dto.PolicySearchDto;
import com.hmg.role.rbac.policy.dto.UpdateBulkPolicyDto;
import com.hmg.role.rbac.policy.dto.UpdatePolicyDto;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import com.hmg.role.util.dto.ListResponseDto;
import java.util.List;

public interface PolicyService {

    ListResponseDto<PolicyDto> getAllPolicies(PolicySearchDto policySearchDto);

    PolicyDto getPolicyByKey(String policyKey);

    PolicyDto createPolicy(CreatePolicyDto createPolicyDto);

    ListResponseDto<PolicyDto> createBulkPolicies(List<CreatePolicyDto> createPolicyDtos);

    PolicyDto updatePolicy(String policyKey, UpdatePolicyDto updatePolicyDto);

    ListResponseDto<PolicyDto> updateBulkPolicies(List<UpdateBulkPolicyDto> updateBulkPolicyDtos);

    void deletePolicy(String policyKey);

    void deleteBulkPolicies(DeleteBulkPolicyDto deleteBulkPolicyDto);

    void deletePolicyItems(List<PolicyItem> policyItems);
}
