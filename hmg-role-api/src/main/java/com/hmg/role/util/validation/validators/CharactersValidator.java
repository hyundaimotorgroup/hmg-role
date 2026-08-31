package com.hmg.role.util.validation.validators;

import com.hmg.role.util.validation.annotations.ValidCharacters;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CharactersValidator implements ConstraintValidator<ValidCharacters, String> {
    private Pattern allowablePattern;

    @Override
    public void initialize(ValidCharacters constraintAnnotation) {
        String patterns =
                Arrays.stream(constraintAnnotation.allowedCharacters())
                        .map(p -> p.patternString)
                        .collect(Collectors.joining(""));
        this.allowablePattern = Pattern.compile("^[" + patterns + "]*$");
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Matcher matcher = allowablePattern.matcher(value);
        return matcher.matches();
    }
}
