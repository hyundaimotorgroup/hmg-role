package com.hmg.role.abac.userset.dryrun;

import com.hmg.role.abac.logicalexpression.EvaluationResult;
import com.hmg.role.abac.logicalexpression.dto.DryRunDto;

public interface DryRunService {
    EvaluationResult dryRunFromInput(DryRunDto dto);
}
