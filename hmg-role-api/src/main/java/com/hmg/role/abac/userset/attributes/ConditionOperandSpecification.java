package com.hmg.role.abac.userset.attributes;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.admin.project.Project;
import com.hmg.role.util.enums.OperandType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConditionOperandSpecification {

    public static Specification<ConditionOperand> findByKeyLikeAndProjectAndDeletedFalse(
            String key, OperandType type, OperandSubject subject, Project project) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Project filter
            predicates.add(criteriaBuilder.equal(root.get("project"), project));

            // Key filter (null or like)
            if (StringUtils.hasText(key)) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(ConditionOperand.PROP_OPERAND)),
                                criteriaBuilder.lower(criteriaBuilder.literal(key + "%"))));
            }

            // Type filter
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            // Subject filter
            predicates.add(criteriaBuilder.equal(root.get("subject"), subject));

            // Deleted filter
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            // TODO: REFACTOR - misrequirement; fix ordering
            // Apply ordering
            if (query != null) {
                query.orderBy(createCustomOrdering(root, criteriaBuilder));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static List<Order> createCustomOrdering(
            Root<ConditionOperand> root, CriteriaBuilder criteriaBuilder) {
        List<Order> orders = new ArrayList<>();

        // Create the complex case expression for ordering
        Expression<String> operandField = root.get(ConditionOperand.PROP_OPERAND);

        CriteriaBuilder.Case<Integer> caseExpression = criteriaBuilder.selectCase();

        // WHEN operand starts with digit THEN 0
        caseExpression =
                caseExpression.when(
                        criteriaBuilder.isTrue(
                                criteriaBuilder.function(
                                        "REGEXP_LIKE",
                                        Boolean.class,
                                        operandField,
                                        criteriaBuilder.literal("^[0-9]"))),
                        0);

        // WHEN operand starts with letter THEN 1
        caseExpression =
                caseExpression.when(
                        criteriaBuilder.isTrue(
                                criteriaBuilder.function(
                                        "REGEXP_LIKE",
                                        Boolean.class,
                                        operandField,
                                        criteriaBuilder.literal("^[A-Za-z]"))),
                        1);

        // WHEN operand starts with underscore THEN 2
        caseExpression = caseExpression.when(criteriaBuilder.like(operandField, "\\_%", '\\'), 2);

        // WHEN operand starts with dash THEN 3
        caseExpression = caseExpression.when(criteriaBuilder.like(operandField, "-%"), 3);

        // ELSE 4
        Expression<Integer> orderExpression = caseExpression.otherwise(4);

        // Add the case expression as first order criterion
        orders.add(criteriaBuilder.asc(orderExpression));

        // Add operand ascending as second order criterion
        orders.add(criteriaBuilder.asc(operandField));

        return orders;
    }

    public static Specification<ConditionOperand> keyExists(
            String key, OperandType type, OperandSubject subject, Project project) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Project filter
            predicates.add(criteriaBuilder.equal(root.get("project"), project));

            // Exact operand match
            predicates.add(criteriaBuilder.equal(root.get(ConditionOperand.PROP_OPERAND), key));

            // Type filter
            predicates.add(criteriaBuilder.equal(root.get("type"), type));

            // Subject filter
            predicates.add(criteriaBuilder.equal(root.get("subject"), subject));

            // Deleted filter
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
