package com.hmg.role.rbac.scope;

import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

public class ScopeOrderingSpec {
    public static Specification<Scope> withBucketedOrder() {
        return (root, query, cb) -> {
            Expression<Boolean> nameIsDigit =
                    cb.function(
                            "regexp_like",
                            Boolean.class,
                            root.get(Scope.PROP_NAME),
                            cb.literal("^[0-9]"));
            Expression<Boolean> nameIsLatin =
                    cb.function(
                            "regexp_like",
                            Boolean.class,
                            root.get(Scope.PROP_NAME),
                            cb.literal("^[A-Za-z]"));
            Expression<Boolean> nameIsHangul =
                    cb.function(
                            "regexp_like",
                            Boolean.class,
                            root.get(Scope.PROP_NAME),
                            cb.literal("^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]"));

            Expression<Object> nameBucket =
                    cb.selectCase()
                            .when(nameIsDigit, 0)
                            .when(nameIsLatin, 1)
                            .when(nameIsHangul, 2)
                            .when(cb.like(root.get(Scope.PROP_NAME), "\\_%", '\\'), 3)
                            .when(cb.like(root.get(Scope.PROP_NAME), "-%"), 4)
                            .otherwise(9);

            // TODO: REFACTOR - misrequirement; fix ordering
            query.orderBy(cb.asc(nameBucket), cb.asc(root.get(Scope.PROP_NAME)));

            return cb.conjunction();
        };
    }
}
