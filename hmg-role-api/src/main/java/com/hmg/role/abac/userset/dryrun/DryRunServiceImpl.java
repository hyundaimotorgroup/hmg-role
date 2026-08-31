package com.hmg.role.abac.userset.dryrun;

import com.hmg.role.abac.logicalexpression.ConditionEvaluationService;
import com.hmg.role.abac.logicalexpression.EvaluationResult;
import com.hmg.role.abac.logicalexpression.dto.DryRunDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DryRunServiceImpl implements DryRunService {
    private final ConditionEvaluationService evaluator;

    @Override
    public EvaluationResult dryRunFromInput(DryRunDto dto) {
        var result = evaluator.evaluateDryRunDto(dto);
        return result;
    }
}
