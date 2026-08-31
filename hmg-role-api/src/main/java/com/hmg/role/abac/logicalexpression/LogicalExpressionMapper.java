package com.hmg.role.abac.logicalexpression;

import com.hmg.role.abac.logicalexpression.enums.LogicalOperator;
import com.hmg.role.abac.logicalexpression.enums.RelationalOperator;
import com.hmg.role.util.entity.AbstractConditionOperand;
import com.hmg.role.util.enums.ConditionGroupOperator;
import com.hmg.role.util.enums.ConditionOperator;
import org.springframework.stereotype.Component;

@Component
public class LogicalExpressionMapper {
    public ConditionalExpression toConditionalExpression(
            ConditionOperator operator,
            AbstractConditionOperand left,
            AbstractConditionOperand right) {
        var relationOperator = toRelationalOperator(operator);
        var leftOperand = formatOperandExpression(left);
        var rightOperand = formatOperandExpression(right);
        return toConditionalExpression(
                relationOperator, leftOperand.toExpression(), rightOperand.toExpression());
    }

    private static OperandExpression formatOperandExpression(AbstractConditionOperand operand) {
        var conditionOperand = operand.getConditionOperand();
        var operandType = conditionOperand.getType();
        var dataType = conditionOperand.getDataType();
        var operandValue = conditionOperand.getOperand();

        return switch (operandType) {
            case ATTRIBUTE -> new OperandExpression("['%s']".formatted(operandValue));
            case LITERAL ->
                    switch (dataType) {
                        // For LITERAL STRING, wrap in single quotes for SpEL
                        case STRING -> new OperandExpression("'" + operandValue + "'");
                        // For LITERAL NUMBER or BOOLEAN, return as-is
                        case NUMBER, BOOLEAN -> new OperandExpression(operandValue);
                    };
        };
    }

    private static ConditionalExpression toConditionalExpression(
            RelationalOperator operator, String left, String right) {
        ConditionalExpression conditionalExpression = new ConditionalExpression();
        conditionalExpression.setOperator(operator);
        conditionalExpression.setLeft(new OperandExpression(left));
        conditionalExpression.setRight(new OperandExpression(right));

        return conditionalExpression;
    }

    public static LogicalOperator toLogicalOperator(ConditionGroupOperator operator) {
        return switch (operator) {
            case AND -> LogicalOperator.AND;
            case OR -> LogicalOperator.OR;
        };
    }

    private static RelationalOperator toRelationalOperator(ConditionOperator operator) {
        return switch (operator) {
            case EQUALS -> RelationalOperator.EQ;
            case NOT_EQUALS -> RelationalOperator.NQ;
            case LESS_THAN -> RelationalOperator.LT;
            case LESS_THAN_OR_EQUALS -> RelationalOperator.LE;
            case GREATER_THAN -> RelationalOperator.GT;
            case GREATER_THAN_OR_EQUALS -> RelationalOperator.GE;
        };
    }
}
