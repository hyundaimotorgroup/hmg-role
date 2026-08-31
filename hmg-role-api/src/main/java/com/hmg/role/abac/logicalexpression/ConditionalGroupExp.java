package com.hmg.role.abac.logicalexpression;

import com.hmg.role.abac.logicalexpression.enums.LogicalOperator;
import com.hmg.role.abac.logicalexpression.interfaces.Expression;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public class ConditionalGroupExp implements Expression {

    private final LogicalOperator operator;
    private final Collection<ConditionalExpression> conditions;

    @Override
    public String toExpression() {
        return conditions.stream()
                .map(Expression::toExpression)
                .collect(Collectors.joining(" " + operator.getSymbol() + " ", " ", " "));
    }
}
