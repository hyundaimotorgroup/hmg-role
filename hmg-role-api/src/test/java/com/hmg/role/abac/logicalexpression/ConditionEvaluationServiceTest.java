package com.hmg.role.abac.logicalexpression;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hmg.role.abac.common.exceptions.AbacAttributeInvalidTypeException;
import com.hmg.role.abac.common.exceptions.AbacAttributeNullValueException;
import com.hmg.role.abac.logicalexpression.dto.ConditionDto;
import com.hmg.role.abac.logicalexpression.dto.DryRunDto;
import com.hmg.role.abac.logicalexpression.dto.OperandDto;
import com.hmg.role.abac.logicalexpression.enums.LogicalOperator;
import com.hmg.role.util.enums.ConditionGroupOperator;
import com.hmg.role.util.enums.ConditionOperator;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

class ConditionEvaluationServiceTest {

    private ConditionEvaluationService service;
    private LogicalExpressionMapper mapper;

    @BeforeEach
    void setUp() {
        var parser = new SpelExpressionParser();
        var context = new StandardEvaluationContext();
        context.addPropertyAccessor(new MapAccessor(false));
        var evaluator = new LogicalExpressionEvaluatorImpl(parser, context);
        mapper = new LogicalExpressionMapper();
        service = new ConditionEvaluationService(evaluator, mapper);
    }

    // ==================== Helpers ====================

    private static OperandDto attribute(String key, OperandDataType dataType) {
        return OperandDto.builder()
                .operand(key)
                .type(OperandType.ATTRIBUTE)
                .dataType(dataType)
                .build();
    }

    private static OperandDto literal(String value, OperandDataType dataType) {
        return OperandDto.builder()
                .operand(value)
                .type(OperandType.LITERAL)
                .dataType(dataType)
                .build();
    }

    private static ConditionDto condition(OperandDto left, ConditionOperator op, OperandDto right) {
        return ConditionDto.builder().left(left).operator(op).right(right).build();
    }

    private static DryRunDto dryRun(
            ConditionGroupOperator groupOp,
            List<ConditionDto> conditions,
            Map<String, Object> attrs) {
        return new DryRunDto(groupOp, conditions, attrs);
    }

    // ==================== STRING attribute ====================

    @Test
    void evaluateDryRunDto_stringAttribute_equalsMatchingLiteral_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("admin", OperandDataType.STRING))),
                        Map.of("role", "admin"));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_stringAttribute_equalsNonMatchingLiteral_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("admin", OperandDataType.STRING))),
                        Map.of("role", "user"));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_stringAttribute_notEquals_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.NOT_EQUALS,
                                        literal("admin", OperandDataType.STRING))),
                        Map.of("role", "user"));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_stringAttribute_notEquals_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.NOT_EQUALS,
                                        literal("admin", OperandDataType.STRING))),
                        Map.of("role", "admin"));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    // ==================== NUMBER attribute (Integer from Jackson) ====================
    // Regression tests: old code wrapped with T(Double).parseDouble() which NPE'd on Integer input.
    // Fixed to use plain ['key'] bracket notation; SpEL StandardTypeComparator handles it natively.

    @Test
    void evaluateDryRunDto_numberAttribute_jackssonInteger_equalsMatchingLiteral_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("departmentID", OperandDataType.NUMBER),
                                        ConditionOperator.EQUALS,
                                        literal("7", OperandDataType.NUMBER))),
                        Map.of("departmentID", 7)); // Integer, as Jackson deserializes JSON numbers

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_jackssonInteger_equalsNonMatchingLiteral_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("departmentID", OperandDataType.NUMBER),
                                        ConditionOperator.EQUALS,
                                        literal("7", OperandDataType.NUMBER))),
                        Map.of("departmentID", 99));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_notEquals_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("score", OperandDataType.NUMBER),
                                        ConditionOperator.NOT_EQUALS,
                                        literal("50", OperandDataType.NUMBER))),
                        Map.of("score", 99));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_notEquals_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("score", OperandDataType.NUMBER),
                                        ConditionOperator.NOT_EQUALS,
                                        literal("50", OperandDataType.NUMBER))),
                        Map.of("score", 50));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_greaterThan_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("score", OperandDataType.NUMBER),
                                        ConditionOperator.GREATER_THAN,
                                        literal("50", OperandDataType.NUMBER))),
                        Map.of("score", 75));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_greaterThan_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("score", OperandDataType.NUMBER),
                                        ConditionOperator.GREATER_THAN,
                                        literal("50", OperandDataType.NUMBER))),
                        Map.of("score", 30));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_greaterThanOrEquals_equalValue_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("score", OperandDataType.NUMBER),
                                        ConditionOperator.GREATER_THAN_OR_EQUALS,
                                        literal("50", OperandDataType.NUMBER))),
                        Map.of("score", 50));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_greaterThanOrEquals_lesserValue_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("score", OperandDataType.NUMBER),
                                        ConditionOperator.GREATER_THAN_OR_EQUALS,
                                        literal("50", OperandDataType.NUMBER))),
                        Map.of("score", 49));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_lessThan_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("age", OperandDataType.NUMBER),
                                        ConditionOperator.LESS_THAN,
                                        literal("30", OperandDataType.NUMBER))),
                        Map.of("age", 25));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_lessThan_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("age", OperandDataType.NUMBER),
                                        ConditionOperator.LESS_THAN,
                                        literal("30", OperandDataType.NUMBER))),
                        Map.of("age", 35));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_lessThanOrEquals_equalValue_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("age", OperandDataType.NUMBER),
                                        ConditionOperator.LESS_THAN_OR_EQUALS,
                                        literal("30", OperandDataType.NUMBER))),
                        Map.of("age", 30));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_numberAttribute_lessThanOrEquals_greaterValue_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("age", OperandDataType.NUMBER),
                                        ConditionOperator.LESS_THAN_OR_EQUALS,
                                        literal("30", OperandDataType.NUMBER))),
                        Map.of("age", 31));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    // ==================== BOOLEAN attribute (Boolean from Jackson) ====================
    // Regression tests: old code wrapped with T(Boolean).parseBoolean() which NPE'd on Boolean
    // input.

    @Test
    void evaluateDryRunDto_booleanAttribute_jacksonBooleanTrue_equalsLiteralTrue_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("isActive", OperandDataType.BOOLEAN),
                                        ConditionOperator.EQUALS,
                                        literal("true", OperandDataType.BOOLEAN))),
                        Map.of(
                                "isActive",
                                Boolean.TRUE)); // Boolean, as Jackson deserializes JSON booleans

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_booleanAttribute_jacksonBooleanFalse_equalsLiteralTrue_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("isActive", OperandDataType.BOOLEAN),
                                        ConditionOperator.EQUALS,
                                        literal("true", OperandDataType.BOOLEAN))),
                        Map.of("isActive", Boolean.FALSE));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_booleanAttribute_jacksonBooleanFalse_equalsLiteralFalse_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("isActive", OperandDataType.BOOLEAN),
                                        ConditionOperator.EQUALS,
                                        literal("false", OperandDataType.BOOLEAN))),
                        Map.of("isActive", Boolean.FALSE));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_booleanAttribute_notEquals_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("isActive", OperandDataType.BOOLEAN),
                                        ConditionOperator.NOT_EQUALS,
                                        literal("true", OperandDataType.BOOLEAN))),
                        Map.of("isActive", Boolean.FALSE));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_booleanAttribute_notEquals_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("isActive", OperandDataType.BOOLEAN),
                                        ConditionOperator.NOT_EQUALS,
                                        literal("true", OperandDataType.BOOLEAN))),
                        Map.of("isActive", Boolean.TRUE));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    // ==================== AND group ====================

    @Test
    void evaluateDryRunDto_andGroup_allConditionsTrue_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("admin", OperandDataType.STRING)),
                                condition(
                                        attribute("departmentID", OperandDataType.NUMBER),
                                        ConditionOperator.EQUALS,
                                        literal("7", OperandDataType.NUMBER)),
                                condition(
                                        attribute("isActive", OperandDataType.BOOLEAN),
                                        ConditionOperator.EQUALS,
                                        literal("true", OperandDataType.BOOLEAN))),
                        Map.of("role", "admin", "departmentID", 7, "isActive", Boolean.TRUE));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_andGroup_oneConditionFalse_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("admin", OperandDataType.STRING)),
                                condition(
                                        attribute("departmentID", OperandDataType.NUMBER),
                                        ConditionOperator.EQUALS,
                                        literal("7", OperandDataType.NUMBER))),
                        Map.of("role", "admin", "departmentID", 99)); // departmentID mismatch

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_andGroup_allConditionsFalse_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("admin", OperandDataType.STRING)),
                                condition(
                                        attribute("departmentID", OperandDataType.NUMBER),
                                        ConditionOperator.EQUALS,
                                        literal("7", OperandDataType.NUMBER))),
                        Map.of("role", "user", "departmentID", 99));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    // ==================== OR group ====================

    @Test
    void evaluateDryRunDto_orGroup_allConditionsTrue_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.OR,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("admin", OperandDataType.STRING)),
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("manager", OperandDataType.STRING))),
                        Map.of("role", "admin"));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_orGroup_oneConditionTrue_returnsTrue() {
        var dto =
                dryRun(
                        ConditionGroupOperator.OR,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("admin", OperandDataType.STRING)),
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("manager", OperandDataType.STRING))),
                        Map.of("role", "manager"));

        assertTrue(service.evaluateDryRunDto(dto).output());
    }

    @Test
    void evaluateDryRunDto_orGroup_allConditionsFalse_returnsFalse() {
        var dto =
                dryRun(
                        ConditionGroupOperator.OR,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("admin", OperandDataType.STRING)),
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("manager", OperandDataType.STRING))),
                        Map.of("role", "user"));

        assertFalse(service.evaluateDryRunDto(dto).output());
    }

    // ==================== Quoted vs unquoted values in request attributes ====================
    // Jackson deserializes {"departmentID": 7} as Integer but {"departmentID": "7"} as String.
    // SpEL equality String("7") == Integer(7) → ObjectUtils.nullSafeEquals → false (type mismatch).
    // Callers must send numbers and booleans as their native JSON types (unquoted).

    @Test
    void evaluateDryRunDto_numberAttribute_quotedStringValue_throwsInvalidAttributeTypeException() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("departmentID", OperandDataType.NUMBER),
                                        ConditionOperator.EQUALS,
                                        literal("7", OperandDataType.NUMBER))),
                        Map.of("departmentID", "7")); // String, as if sent quoted in JSON

        assertThrows(AbacAttributeInvalidTypeException.class, () -> service.evaluateDryRunDto(dto));
    }

    @Test
    void
            evaluateDryRunDto_booleanAttribute_quotedStringValue_throwsInvalidAttributeTypeException() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("isActive", OperandDataType.BOOLEAN),
                                        ConditionOperator.EQUALS,
                                        literal("true", OperandDataType.BOOLEAN))),
                        Map.of("isActive", "true")); // String, as if sent quoted in JSON

        assertThrows(AbacAttributeInvalidTypeException.class, () -> service.evaluateDryRunDto(dto));
    }

    // ==================== Null attribute (key not present in map) ====================

    @Test
    void evaluateDryRunDto_numberAttribute_missingFromMap_throwsNullValueException() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("departmentID", OperandDataType.NUMBER),
                                        ConditionOperator.EQUALS,
                                        literal("7", OperandDataType.NUMBER))),
                        Map.of("role", "admin")); // "departmentID" absent

        assertThrows(AbacAttributeNullValueException.class, () -> service.evaluateDryRunDto(dto));
    }

    @Test
    void evaluateDryRunDto_booleanAttribute_missingFromMap_throwsNullValueException() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("isActive", OperandDataType.BOOLEAN),
                                        ConditionOperator.EQUALS,
                                        literal("true", OperandDataType.BOOLEAN))),
                        Map.of("role", "admin")); // "isActive" absent

        assertThrows(AbacAttributeNullValueException.class, () -> service.evaluateDryRunDto(dto));
    }

    @Test
    void evaluateDryRunDto_stringAttribute_missingFromMap_throwsNullValueException() {
        var dto =
                dryRun(
                        ConditionGroupOperator.AND,
                        List.of(
                                condition(
                                        attribute("role", OperandDataType.STRING),
                                        ConditionOperator.EQUALS,
                                        literal("admin", OperandDataType.STRING))),
                        Map.of("departmentID", 7)); // "role" absent

        assertThrows(AbacAttributeNullValueException.class, () -> service.evaluateDryRunDto(dto));
    }

    // ==================== evaluateConditionalGroup ====================

    @Test
    void evaluateConditionalGroup_andOperator_allTrue_returnsTrue() {
        var left1 =
                buildUserSetOperandForMapper("role", OperandType.ATTRIBUTE, OperandDataType.STRING);
        var right1 =
                buildUserSetOperandForMapper("admin", OperandType.LITERAL, OperandDataType.STRING);
        var left2 =
                buildUserSetOperandForMapper(
                        "departmentID", OperandType.ATTRIBUTE, OperandDataType.NUMBER);
        var right2 = buildUserSetOperandForMapper("7", OperandType.LITERAL, OperandDataType.NUMBER);

        var exprs =
                List.of(
                        mapper.toConditionalExpression(ConditionOperator.EQUALS, left1, right1),
                        mapper.toConditionalExpression(ConditionOperator.EQUALS, left2, right2));

        var result =
                service.evaluateConditionalGroup(
                        LogicalOperator.AND, exprs, Map.of("role", "admin", "departmentID", 7));

        assertTrue(result.output());
    }

    @Test
    void evaluateConditionalGroup_andOperator_oneFalse_returnsFalse() {
        var left1 =
                buildUserSetOperandForMapper("role", OperandType.ATTRIBUTE, OperandDataType.STRING);
        var right1 =
                buildUserSetOperandForMapper("admin", OperandType.LITERAL, OperandDataType.STRING);
        var left2 =
                buildUserSetOperandForMapper(
                        "departmentID", OperandType.ATTRIBUTE, OperandDataType.NUMBER);
        var right2 = buildUserSetOperandForMapper("7", OperandType.LITERAL, OperandDataType.NUMBER);

        var exprs =
                List.of(
                        mapper.toConditionalExpression(ConditionOperator.EQUALS, left1, right1),
                        mapper.toConditionalExpression(ConditionOperator.EQUALS, left2, right2));

        var result =
                service.evaluateConditionalGroup(
                        LogicalOperator.AND, exprs, Map.of("role", "admin", "departmentID", 99));

        assertFalse(result.output());
    }

    @Test
    void evaluateConditionalGroup_orOperator_oneTrue_returnsTrue() {
        var left1 =
                buildUserSetOperandForMapper("role", OperandType.ATTRIBUTE, OperandDataType.STRING);
        var right1 =
                buildUserSetOperandForMapper("admin", OperandType.LITERAL, OperandDataType.STRING);
        var left2 =
                buildUserSetOperandForMapper("role", OperandType.ATTRIBUTE, OperandDataType.STRING);
        var right2 =
                buildUserSetOperandForMapper(
                        "manager", OperandType.LITERAL, OperandDataType.STRING);

        var exprs =
                List.of(
                        mapper.toConditionalExpression(ConditionOperator.EQUALS, left1, right1),
                        mapper.toConditionalExpression(ConditionOperator.EQUALS, left2, right2));

        var result =
                service.evaluateConditionalGroup(
                        LogicalOperator.OR, exprs, Map.of("role", "manager"));

        assertTrue(result.output());
    }

    @Test
    void evaluateConditionalGroup_orOperator_allFalse_returnsFalse() {
        var left1 =
                buildUserSetOperandForMapper("role", OperandType.ATTRIBUTE, OperandDataType.STRING);
        var right1 =
                buildUserSetOperandForMapper("admin", OperandType.LITERAL, OperandDataType.STRING);
        var left2 =
                buildUserSetOperandForMapper("role", OperandType.ATTRIBUTE, OperandDataType.STRING);
        var right2 =
                buildUserSetOperandForMapper(
                        "manager", OperandType.LITERAL, OperandDataType.STRING);

        var exprs =
                List.of(
                        mapper.toConditionalExpression(ConditionOperator.EQUALS, left1, right1),
                        mapper.toConditionalExpression(ConditionOperator.EQUALS, left2, right2));

        var result =
                service.evaluateConditionalGroup(LogicalOperator.OR, exprs, Map.of("role", "user"));

        assertFalse(result.output());
    }

    // ==================== Helper to build AbstractConditionOperand for mapper ====================

    private static com.hmg.role.util.entity.AbstractConditionOperand buildUserSetOperandForMapper(
            String operandValue, OperandType type, OperandDataType dataType) {
        var co = new com.hmg.role.abac.userset.attributes.ConditionOperand();
        co.setOperand(operandValue);
        co.setType(type);
        co.setDataType(dataType);
        return new com.hmg.role.util.entity.AbstractConditionOperand() {
            {
                setConditionOperand(co);
            }
        };
    }
}
