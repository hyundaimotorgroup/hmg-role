package com.hmg.role.api.rbac;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.rbac.policy.dto.CreatePolicyDto;
import com.hmg.role.rbac.policy.dto.DeleteBulkPolicyDto;
import com.hmg.role.rbac.policy.dto.PolicyDto;
import com.hmg.role.rbac.policy.dto.PolicySearchDto;
import com.hmg.role.rbac.policy.dto.UpdateBulkPolicyDto;
import com.hmg.role.rbac.policy.dto.UpdatePolicyDto;
import com.hmg.role.rbac.policy.interfaces.PolicyService;
import com.hmg.role.util.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

@Tag(name = "Policy")
@RequiredArgsConstructor
@RequestMapping("/api/rbac/v1/policies")
@RestController
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "create new policy")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PolicyDto createPolicy(@RequestBody @Valid CreatePolicyDto createPolicyDto) {
        return policyService.createPolicy(createPolicyDto);
    }

    // the purpose of using boolean multipleFlag is to show on swagger-ui dropdown selection
    @Operation(summary = "Bulk Create New Policies")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(params = "multiple=true")
    public ListResponseDto<PolicyDto> createBulkPolicies(
            @RequestParam(name = "multiple") Boolean multipleFlag,
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull CreatePolicyDto> createPolicyDtos) {
        return policyService.createBulkPolicies(createPolicyDtos);
    }

    @Operation(summary = "list policy")
    @GetMapping
    public ListResponseDto<PolicyDto> listPolicy(
            @ParameterObject @ModelAttribute @Valid PolicySearchDto policySearchDto) {
        return policyService.getAllPolicies(policySearchDto);
    }

    @Operation(summary = "get policy by key")
    @GetMapping("/{policyKey}")
    public PolicyDto getPolicyByKey(@PathVariable String policyKey) {
        String decodePolicyKey = URLDecoder.decode(policyKey, StandardCharsets.UTF_8);
        return policyService.getPolicyByKey(decodePolicyKey);
    }

    @Operation(summary = "update existing policy")
    @PutMapping("/{policyKey}")
    public PolicyDto updatePolicy(
            @PathVariable String policyKey, @RequestBody @Valid UpdatePolicyDto updatePolicyDto) {
        String decodePolicyKey = URLDecoder.decode(policyKey, StandardCharsets.UTF_8);
        return policyService.updatePolicy(decodePolicyKey, updatePolicyDto);
    }

    @Operation(summary = "Bulk Update Existing Policies")
    @PutMapping(params = "multiple=true")
    public ListResponseDto<PolicyDto> updateBulkPolicies(
            @RequestParam(name = "multiple") Boolean multipleFlag,
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull UpdateBulkPolicyDto> updateBulkPolicyDtos) {
        return policyService.updateBulkPolicies(updateBulkPolicyDtos);
    }

    @Operation(summary = "delete existing policy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{policyKey}")
    public void deletePolicy(@PathVariable String policyKey) {
        String decodePolicyKey = URLDecoder.decode(policyKey, StandardCharsets.UTF_8);
        policyService.deletePolicy(decodePolicyKey);
    }

    @Operation(summary = "Bulk Delete Existing Policy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(params = "multiple=true")
    public void deletePolicy(
            @RequestParam(name = "multiple") Boolean multipleFlag,
            @RequestBody @Valid DeleteBulkPolicyDto deleteBulkPolicyDto) {
        policyService.deleteBulkPolicies(deleteBulkPolicyDto);
    }
}
