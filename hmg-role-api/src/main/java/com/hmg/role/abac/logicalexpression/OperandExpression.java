package com.hmg.role.abac.logicalexpression;

import com.hmg.role.abac.logicalexpression.interfaces.Expression;

public record OperandExpression(String operand) implements Expression {
    @Override
    public String toExpression() {
        return operand;
    }
}
