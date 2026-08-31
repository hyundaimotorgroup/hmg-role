package com.hmg.role.api.abac;

import com.hmg.role.rbac.scope.dto.CreateScopeDto;
import com.hmg.role.rbac.scope.dto.ScopeDto;
import com.hmg.role.rbac.scope.dto.UpdateScopeDto;
import com.hmg.role.rbac.scope.interfaces.ScopeService;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Tag(name = "ABAC - Scope")
@RestController
@RequestMapping("/api/abac/v1/scopes")
public class AbacScopeController {

    private final ScopeService scopeService;

    public AbacScopeController(
            // necessary to distinguish between the impl beans
            // required because the business logic is ridiculous
            // TODO refactor these when we have a time
            @Qualifier("abacScopeServiceImpl") ScopeService scopeService) {
        this.scopeService = scopeService;
    }

    @Operation(summary = "Create new Scope")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScopeDto createScope(@Valid @RequestBody CreateScopeDto createScopeDto) {
        return scopeService.create(createScopeDto);
    }

    @Operation(summary = "Get Scope By Key")
    @GetMapping("/{scopeKey}")
    public ScopeDto getScopeByKey(@PathVariable String scopeKey) {
        return scopeService.getByKey(scopeKey);
    }

    @Operation(summary = "Get All Scope")
    @GetMapping
    public ListResponseDto<ScopeDto> getAllScope(
            @ParameterObject @ModelAttribute @Valid PageRequestDto pageRequestDto) {
        return scopeService.getAll(pageRequestDto);
    }

    @Operation(summary = "Update Existing Scope")
    @PutMapping("/{scopeKey}")
    public ScopeDto updateScope(
            @PathVariable String scopeKey, @Valid @RequestBody UpdateScopeDto updateDto) {
        return scopeService.update(scopeKey, updateDto);
    }

    @Operation(summary = "Delete existing scope")
    @DeleteMapping("/{scopeKey}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScopeByKey(
            @PathVariable String scopeKey, @RequestParam(required = false) Boolean cascade) {
        if (Boolean.TRUE.equals(cascade)) {
            scopeService.deleteCascadeByKey(scopeKey);
        } else {
            scopeService.deleteByKey(scopeKey);
        }
    }
}
