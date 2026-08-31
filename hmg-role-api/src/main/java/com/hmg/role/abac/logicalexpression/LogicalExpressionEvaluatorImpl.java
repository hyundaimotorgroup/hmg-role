package com.hmg.role.abac.logicalexpression;

import com.hmg.role.abac.logicalexpression.interfaces.Expression;
import com.hmg.role.abac.logicalexpression.interfaces.LogicalExpressionEvaluator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogicalExpressionEvaluatorImpl implements LogicalExpressionEvaluator {

    private final ExpressionParser parser;
    private final StandardEvaluationContext context;

    @Override
    public EvaluationResult evaluate(Expression expression, Map<String, Object> attributes) {
        var expressionRes = parser.parseExpression(expression.toExpression());
        return EvaluationResult.ofOutput(
                Boolean.TRUE.equals(expressionRes.getValue(context, attributes, Boolean.class)));
    }
}
