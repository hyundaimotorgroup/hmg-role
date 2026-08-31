package com.hmg.role.util.validation.annotations;

import com.hmg.role.util.validation.validators.NoDuplicateValuesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NoDuplicateValuesValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoDuplicateValues {
    String message() default "must not contain duplicate values";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
