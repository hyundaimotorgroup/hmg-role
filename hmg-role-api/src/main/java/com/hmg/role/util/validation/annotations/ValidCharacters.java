package com.hmg.role.util.validation.annotations;

import com.hmg.role.util.enums.CharacterClass;
import com.hmg.role.util.validation.validators.CharactersValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Validate characters in the annotated String. */
@Constraint(validatedBy = CharactersValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCharacters {
    /** Characters allowed to be entered to the validated String */
    CharacterClass[] allowedCharacters();

    String message() default "should contain only valid elements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
