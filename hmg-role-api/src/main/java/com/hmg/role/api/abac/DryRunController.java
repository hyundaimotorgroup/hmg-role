package com.hmg.role.api.abac;

import com.hmg.role.abac.logicalexpression.EvaluationResult;
import com.hmg.role.abac.logicalexpression.dto.DryRunDto;
import com.hmg.role.abac.userset.dryrun.DryRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DryRunController")
@RequiredArgsConstructor
@RequestMapping("/api/abac/v1/condition-dry-run")
@RestController
public class DryRunController {
    private final DryRunService dryRunService;

    @Operation(summary = "Dry run condition from user input")
    @PostMapping
    EvaluationResult dryRunFromInput(@Valid @RequestBody DryRunDto dto) {
        return dryRunService.dryRunFromInput(dto);
    }
}
