package com.hmg.role.api.abac;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.abac.userset.attributes.AbacAttributeService;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeDeleteDto;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeDto;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeSearchDto;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tags({@Tag(name = "ABAC user set attributes")})
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/abac/v1/user-set-attributes")
public class UserSetAttributeController {
    private final AbacAttributeService service;

    @Operation(summary = "Get all user set attributes")
    @GetMapping
    public ListResponseDto<ConditionAttributeDto> getAll(
            @ParameterObject @ModelAttribute @Valid ConditionAttributeSearchDto params) {
        return service.getAll(params, OperandSubject.USER_SET);
    }

    @Operation(summary = "Insert new user set attribute")
    @PostMapping
    public ConditionAttributeDto insert(@RequestBody @Valid ConditionAttributeDto operandDto) {
        return service.create(operandDto, OperandSubject.USER_SET);
    }

    @Operation(summary = "Delete a user set attribute")
    @DeleteMapping
    public void delete(@RequestBody @Valid ConditionAttributeDeleteDto operandDto) {
        service.delete(operandDto, OperandSubject.USER_SET);
    }

    @Operation(summary = "Get all user set operand types")
    @GetMapping("/type-parameters")
    public Object getAttributeParameterProperties() {
        return Map.ofEntries(
                Map.entry("type", Arrays.stream(OperandType.values()).map(Enum::toString).toList()),
                Map.entry(
                        "dataType",
                        Arrays.stream(OperandDataType.values()).map(Enum::toString).toList()));
    }
}
