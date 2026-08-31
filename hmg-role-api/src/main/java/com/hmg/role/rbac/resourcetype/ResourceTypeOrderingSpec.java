package com.hmg.role.rbac.resourcetype;

import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

public class ResourceTypeOrderingSpec {
    public static Specification<ResourceType> withBucketedOrder() {
        return (root, query, cb) -> {
            Expression<Boolean> nameIsDigit =
                    cb.function(
                            "regexp_like", Boolean.class, root.get("name"), cb.literal("^[0-9]"));
            Expression<Boolean> nameIsLatin =
                    cb.function(
                            "regexp_like",
                            Boolean.class,
                            root.get("name"),
                            cb.literal("^[A-Za-z]"));
            Expression<Boolean> nameIsHangul =
                    cb.function(
                            "regexp_like",
                            Boolean.class,
                            root.get("name"),
                            cb.literal("^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]"));

            Expression<Object> nameBucket =
                    cb.selectCase()
                            .when(nameIsDigit, 0)
                            .when(nameIsLatin, 1)
                            .when(nameIsHangul, 2)
                            .when(cb.like(root.get("name"), "\\_%", '\\'), 3)
                            .when(cb.like(root.get("name"), "-%"), 4)
                            .otherwise(9);

            Expression<Boolean> keyIsDigit =
                    cb.function(
                            "regexp_like", Boolean.class, root.get("key"), cb.literal("^[0-9]"));
            Expression<Boolean> keyIsLatin =
                    cb.function(
                            "regexp_like", Boolean.class, root.get("key"), cb.literal("^[A-Za-z]"));
            Expression<Boolean> keyIsHangul =
                    cb.function(
                            "regexp_like",
                            Boolean.class,
                            root.get("key"),
                            cb.literal("^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]"));

            Expression<Object> keyBucket =
                    cb.selectCase()
                            .when(keyIsDigit, 0)
                            .when(keyIsLatin, 1)
                            .when(keyIsHangul, 2)
                            .when(cb.like(root.get("key"), "\\_%", '\\'), 3)
                            .when(cb.like(root.get("key"), "-%"), 4)
                            .otherwise(9);

            query.orderBy(
                    cb.asc(nameBucket),
                    cb.asc(root.get("name")),
                    cb.asc(keyBucket),
                    cb.asc(root.get("key")));

            return cb.conjunction();
        };
    }
}
