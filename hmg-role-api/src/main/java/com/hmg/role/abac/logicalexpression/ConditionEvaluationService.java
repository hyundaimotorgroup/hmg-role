package com.hmg.role.abac.logicalexpression;

import com.hmg.role.abac.common.exceptions.AbacAttributeInvalidTypeException;
import com.hmg.role.abac.common.exceptions.AbacAttributeNullValueException;
import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.abac.logicalexpression.dto.DryRunDto;
import com.hmg.role.abac.logicalexpression.dto.OperandDto;
import com.hmg.role.abac.logicalexpression.enums.LogicalOperator;
import com.hmg.role.abac.logicalexpression.interfaces.LogicalExpressionEvaluator;
import com.hmg.role.abac.userset.attributes.ConditionOperand;
import com.hmg.role.util.entity.AbstractConditionOperand;
import com.hmg.role.util.enums.OperandType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating logical expressions and conditions against attribute sets. This service
 * provides multiple levels of abstraction for condition evaluation: - Low-level: Direct evaluation
 * of conditional groups - High-level: Evaluation of specific entity types (UserSet, ResourceSet).
 * This class was vibe coded
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConditionEvaluationService {

    private final LogicalExpressionEvaluator logicalExpressionEvaluator;
    private final LogicalExpressionMapper logicalExpressionMapper;

    public EvaluationResult evaluateDryRunDto(DryRunDto evalDto) {
        var userAttributes = evalDto.attributeValues();
        for (var condition : evalDto.conditionGroup()) {
            validateOperandType(condition.left(), userAttributes);
            validateOperandType(condition.right(), userAttributes);
        }
        List<ConditionalExpression> conditionalExpressions =
                evalDto.conditionGroup().stream().map(this::toConditionalExpression).toList();
        var logicalOperator =
                LogicalExpressionMapper.toLogicalOperator(evalDto.conditionGroupOperator());
        return evaluateConditionalGroup(logicalOperator, conditionalExpressions, userAttributes);
    }

    public EvaluationResult evaluateConditionalGroup(
            LogicalOperator logicalOperator,
            List<ConditionalExpression> conditionalExpressions,
            Map<String, Object> attributes) {
        ConditionalGroupExp group =
                ConditionalGroupExp.builder()
                        .operator(logicalOperator)
                        .conditions(conditionalExpressions)
                        .build();
        return logicalExpressionEvaluator.evaluate(group, attributes);
    }

    private ConditionalExpression toConditionalExpression(ConditionDto conditionDto) {
        // TODO  move to mapper
        return logicalExpressionMapper.toConditionalExpression(
                conditionDto.operator(),
                toOperand(conditionDto.left()),
                toOperand(conditionDto.right()));
    }

    private static void validateOperandType(OperandDto operand, Map<String, Object> attributes) {
        if (operand.type() != OperandType.ATTRIBUTE) return;
        var value =
                Optional.ofNullable(attributes.get(operand.operand()))
                        .orElseThrow(() -> new AbacAttributeNullValueException(operand.operand()));
        var dataType = operand.dataType();
        var valid =
                switch (dataType) {
                    case NUMBER -> value instanceof Number;
                    case BOOLEAN -> value instanceof Boolean;
                    case STRING -> true;
                };
        if (!valid) {
            throw new AbacAttributeInvalidTypeException(operand.operand(), dataType);
        }
    }

    private static AbstractConditionOperand toOperand(OperandDto dto) {
        // TODO move to mapper
        AbstractConditionOperand res =
                new AbstractConditionOperand() {
                    {
                        setConditionOperand(
                                new ConditionOperand() {
                                    {
                                        setOperand(dto.operand());
                                        setType(dto.type());
                                        setDataType(dto.dataType());
                                    }
                                });
                    }
                };

        return res;
    }
}
