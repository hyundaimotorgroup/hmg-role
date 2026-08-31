package com.hmg.role.abac.logicalexpression;

import com.hmg.role.abac.logicalexpression.enums.RelationalOperator;
import com.hmg.role.abac.logicalexpression.interfaces.Expression;
import lombok.Setter;

@Setter
public class ConditionalExpression implements Expression {

    private Expression left;
    private Expression right;
    private RelationalOperator operator;

    @Override
    public String toExpression() {
        return left.toExpression() + " " + operator.getSymbol() + " " + right.toExpression();
    }
}
