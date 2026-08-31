package com.hmg.role.abac.logicalexpression;

import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.*;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildConditionOperand;
import static com.hmg.role.abac.permission.util.AbacPermissionTestDataBuilder.buildUserSetOperand;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hmg.role.abac.logicalexpression.interfaces.LogicalExpressionEvaluator;
import com.hmg.role.util.enums.ConditionOperator;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandPosition;
import com.hmg.role.util.enums.OperandType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

class LogicalExpressionMapperTest {

    private LogicalExpressionMapper mapper;
    private LogicalExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        mapper = new LogicalExpressionMapper();
        var parser = new SpelExpressionParser();
        var context = new StandardEvaluationContext();
        context.addPropertyAccessor(new MapAccessor(false));
        evaluator = new LogicalExpressionEvaluatorImpl(parser, context);
    }

    // ==================== Expression String Generation ====================

    @Test
    void toConditionalExpression_attributeNumber_generatesPlainBracketNotation() {
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.NUMBER, "departmentID"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(OperandType.LITERAL, OperandDataType.NUMBER, "7"));

        var expr = mapper.toConditionalExpression(ConditionOperator.EQUALS, left, right);

        assertEquals("['departmentID'] == 7", expr.toExpression());
    }

    @Test
    void toConditionalExpression_attributeBoolean_generatesPlainBracketNotation() {
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.BOOLEAN, "isActive"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(
                                OperandType.LITERAL, OperandDataType.BOOLEAN, "true"));

        var expr = mapper.toConditionalExpression(ConditionOperator.EQUALS, left, right);

        assertEquals("['isActive'] == true", expr.toExpression());
    }

    @Test
    void toConditionalExpression_attributeString_generatesPlainBracketNotation() {
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.STRING, "role"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(
                                OperandType.LITERAL, OperandDataType.STRING, "admin"));

        var expr = mapper.toConditionalExpression(ConditionOperator.EQUALS, left, right);

        assertEquals("['role'] == 'admin'", expr.toExpression());
    }

    // ==================== Integration: Jackson-deserialized types ====================

    @Test
    void
            toConditionalExpression_attributeNumber_jacksonDeserializedInteger_evaluatesWithoutError() {
        // Jackson deserializes JSON {"departmentID": 7} as Integer(7), not String("7").
        // The old T(Double).parseDouble(['departmentID']) would NPE on Integer input.
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.NUMBER, "departmentID"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(OperandType.LITERAL, OperandDataType.NUMBER, "7"));

        var expr = mapper.toConditionalExpression(ConditionOperator.EQUALS, left, right);
        Map<String, Object> attributes = Map.of("departmentID", 7); // Integer, not String

        assertDoesNotThrow(() -> evaluator.evaluate(expr, attributes));
        assertTrue(evaluator.evaluate(expr, attributes).output());
    }

    @Test
    void
            toConditionalExpression_attributeNumber_jacksonDeserializedInteger_notMatchingLiteral_returnsFalse() {
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.NUMBER, "departmentID"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(OperandType.LITERAL, OperandDataType.NUMBER, "99"));

        var expr = mapper.toConditionalExpression(ConditionOperator.EQUALS, left, right);
        Map<String, Object> attributes = Map.of("departmentID", 7);

        assertFalse(evaluator.evaluate(expr, attributes).output());
    }

    @Test
    void
            toConditionalExpression_attributeBoolean_jacksonDeserializedBoolean_evaluatesWithoutError() {
        // Jackson deserializes JSON {"isActive": true} as Boolean.TRUE, not String("true").
        // The old T(Boolean).parseBoolean(['isActive']) would NPE on Boolean input.
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.BOOLEAN, "isActive"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(
                                OperandType.LITERAL, OperandDataType.BOOLEAN, "true"));

        var expr = mapper.toConditionalExpression(ConditionOperator.EQUALS, left, right);
        Map<String, Object> attributes = Map.of("isActive", Boolean.TRUE); // Boolean, not String

        assertDoesNotThrow(() -> evaluator.evaluate(expr, attributes));
        assertTrue(evaluator.evaluate(expr, attributes).output());
    }

    @Test
    void
            toConditionalExpression_attributeBoolean_jacksonDeserializedBoolean_notMatchingLiteral_returnsFalse() {
        var left =
                buildUserSetOperand(
                        OperandPosition.LEFT,
                        buildConditionOperand(
                                OperandType.ATTRIBUTE, OperandDataType.BOOLEAN, "isActive"));
        var right =
                buildUserSetOperand(
                        OperandPosition.RIGHT,
                        buildConditionOperand(
                                OperandType.LITERAL, OperandDataType.BOOLEAN, "true"));

        var expr = mapper.toConditionalExpression(ConditionOperator.EQUALS, left, right);
        Map<String, Object> attributes = Map.of("isActive", Boolean.FALSE);

        assertFalse(evaluator.evaluate(expr, attributes).output());
    }
}
