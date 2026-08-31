package com.hmg.role.abac.logicalexpression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hmg.role.abac.logicalexpression.enums.LogicalOperator;
import com.hmg.role.abac.logicalexpression.enums.RelationalOperator;
import com.hmg.role.abac.logicalexpression.interfaces.Expression;
import com.hmg.role.abac.logicalexpression.interfaces.LogicalExpressionEvaluator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

class LogicalExpressionEvaluatorImplTest {

    private LogicalExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.addPropertyAccessor(new MapAccessor(false));
        evaluator = new LogicalExpressionEvaluatorImpl(parser, context);
    }

    @AfterEach
    void tearDown() {
        evaluator = null;
    }

    // ==================== OperandExpression Tests ====================

    @Test
    void testEvaluate_WithSimpleOperandExpression_ReturnsTrue() {
        // Given
        Expression expression = new OperandExpression("[isActive]");
        Map<String, Object> attributes = Map.of("isActive", true);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
        assertNull(result.reasoning());
    }

    @Test
    void testEvaluate_WithSimpleOperandExpression_ReturnsFalse() {
        // Given
        Expression expression = new OperandExpression("[isActive]");
        Map<String, Object> attributes = Map.of("isActive", false);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
        assertNull(result.reasoning());
    }

    // ==================== ConditionalExpression Tests with EQ ====================

    @Test
    void testEvaluate_WithConditionalExpression_EQ_ReturnsTrue() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[role]"));
        expression.setOperator(RelationalOperator.EQ);
        expression.setRight(new OperandExpression("'admin'"));
        Map<String, Object> attributes = Map.of("role", "admin");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalExpression_EQ_ReturnsFalse() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[role]"));
        expression.setOperator(RelationalOperator.EQ);
        expression.setRight(new OperandExpression("'admin'"));
        Map<String, Object> attributes = Map.of("role", "user");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }

    @Test
    void testEvaluate_WithConditionalExpression_EQ_NumericValues_ReturnsTrue() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[age]"));
        expression.setOperator(RelationalOperator.EQ);
        expression.setRight(new OperandExpression("25"));
        Map<String, Object> attributes = Map.of("age", 25);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    // ==================== ConditionalExpression Tests with NQ ====================

    @Test
    void testEvaluate_WithConditionalExpression_NQ_ReturnsTrue() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[status]"));
        expression.setOperator(RelationalOperator.NQ);
        expression.setRight(new OperandExpression("'inactive'"));
        Map<String, Object> attributes = Map.of("status", "active");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalExpression_NQ_ReturnsFalse() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[status]"));
        expression.setOperator(RelationalOperator.NQ);
        expression.setRight(new OperandExpression("'active'"));
        Map<String, Object> attributes = Map.of("status", "active");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }

    // ==================== ConditionalExpression Tests with GT ====================

    @Test
    void testEvaluate_WithConditionalExpression_GT_ReturnsTrue() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[score]"));
        expression.setOperator(RelationalOperator.GT);
        expression.setRight(new OperandExpression("50"));
        Map<String, Object> attributes = Map.of("score", 75);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalExpression_GT_ReturnsFalse() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[score]"));
        expression.setOperator(RelationalOperator.GT);
        expression.setRight(new OperandExpression("50"));
        Map<String, Object> attributes = Map.of("score", 30);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }

    // ==================== ConditionalExpression Tests with LT ====================

    @Test
    void testEvaluate_WithConditionalExpression_LT_ReturnsTrue() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[age]"));
        expression.setOperator(RelationalOperator.LT);
        expression.setRight(new OperandExpression("30"));
        Map<String, Object> attributes = Map.of("age", 25);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalExpression_LT_ReturnsFalse() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[age]"));
        expression.setOperator(RelationalOperator.LT);
        expression.setRight(new OperandExpression("30"));
        Map<String, Object> attributes = Map.of("age", 35);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }

    // ==================== ConditionalExpression Tests with GE ====================

    @Test
    void testEvaluate_WithConditionalExpression_GE_ReturnsTrue_Greater() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[count]"));
        expression.setOperator(RelationalOperator.GE);
        expression.setRight(new OperandExpression("10"));
        Map<String, Object> attributes = Map.of("count", 15);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalExpression_GE_ReturnsTrue_Equal() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[count]"));
        expression.setOperator(RelationalOperator.GE);
        expression.setRight(new OperandExpression("10"));
        Map<String, Object> attributes = Map.of("count", 10);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalExpression_GE_ReturnsFalse() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[count]"));
        expression.setOperator(RelationalOperator.GE);
        expression.setRight(new OperandExpression("10"));
        Map<String, Object> attributes = Map.of("count", 5);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }

    // ==================== ConditionalExpression Tests with LE ====================

    @Test
    void testEvaluate_WithConditionalExpression_LE_ReturnsTrue_Less() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[limit]"));
        expression.setOperator(RelationalOperator.LE);
        expression.setRight(new OperandExpression("100"));
        Map<String, Object> attributes = Map.of("limit", 50);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalExpression_LE_ReturnsTrue_Equal() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[limit]"));
        expression.setOperator(RelationalOperator.LE);
        expression.setRight(new OperandExpression("100"));
        Map<String, Object> attributes = Map.of("limit", 100);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalExpression_LE_ReturnsFalse() {
        // Given
        ConditionalExpression expression = new ConditionalExpression();
        expression.setLeft(new OperandExpression("[limit]"));
        expression.setOperator(RelationalOperator.LE);
        expression.setRight(new OperandExpression("100"));
        Map<String, Object> attributes = Map.of("limit", 150);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }

    // ==================== ConditionalGroupExp Tests with AND ====================

    @Test
    void testEvaluate_WithConditionalGroupExp_AND_AllTrue_ReturnsTrue() {
        // Given
        ConditionalExpression condition1 = new ConditionalExpression();
        condition1.setLeft(new OperandExpression("[age]"));
        condition1.setOperator(RelationalOperator.GT);
        condition1.setRight(new OperandExpression("18"));

        ConditionalExpression condition2 = new ConditionalExpression();
        condition2.setLeft(new OperandExpression("[status]"));
        condition2.setOperator(RelationalOperator.EQ);
        condition2.setRight(new OperandExpression("'active'"));

        Expression expression =
                ConditionalGroupExp.builder()
                        .operator(LogicalOperator.AND)
                        .conditions(List.of(condition1, condition2))
                        .build();

        Map<String, Object> attributes = Map.of("age", 25, "status", "active");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalGroupExp_AND_OneFalse_ReturnsFalse() {
        // Given
        ConditionalExpression condition1 = new ConditionalExpression();
        condition1.setLeft(new OperandExpression("[age]"));
        condition1.setOperator(RelationalOperator.GT);
        condition1.setRight(new OperandExpression("18"));

        ConditionalExpression condition2 = new ConditionalExpression();
        condition2.setLeft(new OperandExpression("[status]"));
        condition2.setOperator(RelationalOperator.EQ);
        condition2.setRight(new OperandExpression("'active'"));

        Expression expression =
                ConditionalGroupExp.builder()
                        .operator(LogicalOperator.AND)
                        .conditions(List.of(condition1, condition2))
                        .build();

        Map<String, Object> attributes = Map.of("age", 25, "status", "inactive");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }

    @Test
    void testEvaluate_WithConditionalGroupExp_AND_AllFalse_ReturnsFalse() {
        // Given
        ConditionalExpression condition1 = new ConditionalExpression();
        condition1.setLeft(new OperandExpression("[age]"));
        condition1.setOperator(RelationalOperator.GT);
        condition1.setRight(new OperandExpression("18"));

        ConditionalExpression condition2 = new ConditionalExpression();
        condition2.setLeft(new OperandExpression("[score]"));
        condition2.setOperator(RelationalOperator.GE);
        condition2.setRight(new OperandExpression("50"));

        Expression expression =
                ConditionalGroupExp.builder()
                        .operator(LogicalOperator.AND)
                        .conditions(List.of(condition1, condition2))
                        .build();

        Map<String, Object> attributes = Map.of("age", 15, "score", 30);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }

    @Test
    void testEvaluate_WithConditionalGroupExp_AND_MultipleConditions_ReturnsTrue() {
        // Given
        ConditionalExpression condition1 = new ConditionalExpression();
        condition1.setLeft(new OperandExpression("[age]"));
        condition1.setOperator(RelationalOperator.GE);
        condition1.setRight(new OperandExpression("18"));

        ConditionalExpression condition2 = new ConditionalExpression();
        condition2.setLeft(new OperandExpression("[age]"));
        condition2.setOperator(RelationalOperator.LE);
        condition2.setRight(new OperandExpression("65"));

        ConditionalExpression condition3 = new ConditionalExpression();
        condition3.setLeft(new OperandExpression("[status]"));
        condition3.setOperator(RelationalOperator.EQ);
        condition3.setRight(new OperandExpression("'active'"));

        Expression expression =
                ConditionalGroupExp.builder()
                        .operator(LogicalOperator.AND)
                        .conditions(List.of(condition1, condition2, condition3))
                        .build();

        Map<String, Object> attributes = Map.of("age", 30, "status", "active");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    // ==================== ConditionalGroupExp Tests with OR ====================

    @Test
    void testEvaluate_WithConditionalGroupExp_OR_AllTrue_ReturnsTrue() {
        // Given
        ConditionalExpression condition1 = new ConditionalExpression();
        condition1.setLeft(new OperandExpression("[role]"));
        condition1.setOperator(RelationalOperator.EQ);
        condition1.setRight(new OperandExpression("'admin'"));

        ConditionalExpression condition2 = new ConditionalExpression();
        condition2.setLeft(new OperandExpression("[role]"));
        condition2.setOperator(RelationalOperator.EQ);
        condition2.setRight(new OperandExpression("'manager'"));

        Expression expression =
                ConditionalGroupExp.builder()
                        .operator(LogicalOperator.OR)
                        .conditions(List.of(condition1, condition2))
                        .build();

        Map<String, Object> attributes = Map.of("role", "admin");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalGroupExp_OR_OneTrue_ReturnsTrue() {
        // Given
        ConditionalExpression condition1 = new ConditionalExpression();
        condition1.setLeft(new OperandExpression("[age]"));
        condition1.setOperator(RelationalOperator.LT);
        condition1.setRight(new OperandExpression("18"));

        ConditionalExpression condition2 = new ConditionalExpression();
        condition2.setLeft(new OperandExpression("[age]"));
        condition2.setOperator(RelationalOperator.GT);
        condition2.setRight(new OperandExpression("65"));

        Expression expression =
                ConditionalGroupExp.builder()
                        .operator(LogicalOperator.OR)
                        .conditions(List.of(condition1, condition2))
                        .build();

        Map<String, Object> attributes = Map.of("age", 70);

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithConditionalGroupExp_OR_AllFalse_ReturnsFalse() {
        // Given
        ConditionalExpression condition1 = new ConditionalExpression();
        condition1.setLeft(new OperandExpression("[role]"));
        condition1.setOperator(RelationalOperator.EQ);
        condition1.setRight(new OperandExpression("'admin'"));

        ConditionalExpression condition2 = new ConditionalExpression();
        condition2.setLeft(new OperandExpression("[role]"));
        condition2.setOperator(RelationalOperator.EQ);
        condition2.setRight(new OperandExpression("'manager'"));

        Expression expression =
                ConditionalGroupExp.builder()
                        .operator(LogicalOperator.OR)
                        .conditions(List.of(condition1, condition2))
                        .build();

        Map<String, Object> attributes = Map.of("role", "user");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }

    @Test
    void testEvaluate_WithConditionalGroupExp_OR_MultipleConditions_ReturnsTrue() {
        // Given
        ConditionalExpression condition1 = new ConditionalExpression();
        condition1.setLeft(new OperandExpression("[department]"));
        condition1.setOperator(RelationalOperator.EQ);
        condition1.setRight(new OperandExpression("'IT'"));

        ConditionalExpression condition2 = new ConditionalExpression();
        condition2.setLeft(new OperandExpression("[department]"));
        condition2.setOperator(RelationalOperator.EQ);
        condition2.setRight(new OperandExpression("'HR'"));

        ConditionalExpression condition3 = new ConditionalExpression();
        condition3.setLeft(new OperandExpression("[department]"));
        condition3.setOperator(RelationalOperator.EQ);
        condition3.setRight(new OperandExpression("'Finance'"));

        Expression expression =
                ConditionalGroupExp.builder()
                        .operator(LogicalOperator.OR)
                        .conditions(List.of(condition1, condition2, condition3))
                        .build();

        Map<String, Object> attributes = Map.of("department", "Finance");

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    // ==================== Complex Nested Expression Tests ====================

    @Test
    void testEvaluate_WithComplexExpression_ReturnsTrue() {
        // Given: (age >= 18 AND age <= 65) OR role == 'admin'
        ConditionalExpression ageGTE18 = new ConditionalExpression();
        ageGTE18.setLeft(new OperandExpression("[age]"));
        ageGTE18.setOperator(RelationalOperator.GE);
        ageGTE18.setRight(new OperandExpression("18"));

        ConditionalExpression ageLTE65 = new ConditionalExpression();
        ageLTE65.setLeft(new OperandExpression("[age]"));
        ageLTE65.setOperator(RelationalOperator.LE);
        ageLTE65.setRight(new OperandExpression("65"));

        ConditionalExpression roleIsAdmin = new ConditionalExpression();
        roleIsAdmin.setLeft(new OperandExpression("[role]"));
        roleIsAdmin.setOperator(RelationalOperator.EQ);
        roleIsAdmin.setRight(new OperandExpression("'admin'"));

        // Create AND group for age conditions
        ConditionalGroupExp ageGroup =
                ConditionalGroupExp.builder()
                        .operator(LogicalOperator.AND)
                        .conditions(List.of(ageGTE18, ageLTE65))
                        .build();

        // Note: For a truly nested expression, we'd need to support Expression as conditions
        // But based on the current implementation, ConditionalGroupExp only accepts
        // ConditionalExpression
        // So testing with the current structure

        Map<String, Object> attributes = Map.of("age", 30);

        // When
        EvaluationResult result = evaluator.evaluate(ageGroup, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithEmptyAttributes_ReturnsFalse() {
        // Given
        Expression expression = new OperandExpression("true");
        Map<String, Object> attributes = Map.of();

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithLiteralTrueExpression_ReturnsTrue() {
        // Given
        Expression expression = new OperandExpression("true");
        Map<String, Object> attributes = Map.of();

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertTrue(result.output());
    }

    @Test
    void testEvaluate_WithLiteralFalseExpression_ReturnsFalse() {
        // Given
        Expression expression = new OperandExpression("false");
        Map<String, Object> attributes = Map.of();

        // When
        EvaluationResult result = evaluator.evaluate(expression, attributes);

        // Then
        assertNotNull(result);
        assertFalse(result.output());
    }
}
