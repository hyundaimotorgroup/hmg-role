package com.hmg.role.api.abac;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.abac.permission.dto.AbacPolicySearchDto;
import com.hmg.role.abac.policy.dto.AbacPolicyDto;
import com.hmg.role.abac.policy.dto.DeleteBulkAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateBulkAbacPolicyDto;
import com.hmg.role.abac.policy.interfaces.AbacPolicyService;
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
import org.springframework.web.bind.annotation.*;
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

@Tag(name = "ABAC Policy")
@RequiredArgsConstructor
@RequestMapping("/api/abac/v1/policies")
@RestController
public class AbacPolicyController {

    private final AbacPolicyService policyService;

    @Operation(summary = "Create new policy")
    @PostMapping(params = "multiple=false")
    @Parameter(name = "multiple", schema = @Schema(allowableValues = {"true", "false"}))
    public AbacPolicyDto create(@RequestBody @Valid AbacPolicyDto abacPolicyDto) {
        return policyService.createPolicy(abacPolicyDto);
    }

    // multiple variable are flag used only for swagger ui
    @Operation(summary = "Create Bulk New Policies")
    @PostMapping(params = "multiple=true")
    @Parameter(
            name = "multiple",
            schema = @Schema(allowableValues = {"true", "false"}),
            required = true)
    public ListResponseDto<AbacPolicyDto> createBulk(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull AbacPolicyDto> abacPolicyDtos) {
        return policyService.createBulkPolicies(abacPolicyDtos);
    }

    @Operation(summary = "list of policy")
    @GetMapping
    public ListResponseDto<AbacPolicyDto> list(
            @ParameterObject @ModelAttribute @Valid AbacPolicySearchDto abacPolicySearchDto) {
        return policyService.getPolicies(abacPolicySearchDto);
    }

    @Operation(summary = "Get of policy by key")
    @GetMapping("/{key}")
    public AbacPolicyDto getByKey(@PathVariable String key) {
        return policyService.getPolicyByKey(key);
    }

    @Operation(summary = "Update existing policy data")
    @PutMapping("/{key}")
    public AbacPolicyDto update(
            @PathVariable String key, @RequestBody @Valid UpdateAbacPolicyDto updateAbacPolicyDto) {
        return policyService.updatePolicy(key, updateAbacPolicyDto);
    }

    @Operation(summary = "Update Bulk Existing Policies")
    @PutMapping
    public ListResponseDto<AbacPolicyDto> updateBulk(
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull UpdateBulkAbacPolicyDto> updateBulkAbacPolicyDtos) {
        return policyService.updateBulkPolicies(updateBulkAbacPolicyDtos);
    }

    @Operation(summary = "delete existing policy data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{key}")
    public void delete(@PathVariable String key) {
        policyService.deletePolicy(key);
    }

    @Operation(summary = "Delete Bulk Existing Policies")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteBulk(@RequestBody @Valid DeleteBulkAbacPolicyDto deleteBulkAbacPolicyDto) {
        policyService.deleteBulkPolicies(deleteBulkAbacPolicyDto);
    }
}
