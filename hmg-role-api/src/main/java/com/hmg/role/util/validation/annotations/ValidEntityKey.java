package com.hmg.role.util.validation.annotations;

import static com.hmg.role.util.Constants.ABAC_DTO_KEY_REGEX_PATTERN;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotBlank(message = "key is required")
@Pattern(
        regexp = ABAC_DTO_KEY_REGEX_PATTERN,
        message =
                "Key must contain only non-diacritic latin characters (A-Z/a-z),"
                        + " numbers (0-9),"
                        + " hyphens ('-'),"
                        + " underscores ('_'),"
                        + " forward slashes ('/'),"
                        + " equals signs ('='),"
                        + " colons (':'),"
                        + " question marks ('?'),"
                        + " and curly braces ('{'/'}')")
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEntityKey {
    String message() default "key is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
