package com.hmg.role.abac.logicalexpression.interfaces;

import com.hmg.role.abac.logicalexpression.EvaluationResult;
import java.util.Map;

public interface LogicalExpressionEvaluator {
    EvaluationResult evaluate(Expression expression, Map<String, Object> attributes);
}
