package com.hmg.role.util.validation.validators;

import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;

public class NoDuplicateValuesValidator implements ConstraintValidator<NoDuplicateValues, List<?>> {

    @Override
    public boolean isValid(List list, ConstraintValidatorContext context) {
        if (list == null) {
            return true;
        }
        var set = new HashSet<>();
        for (var value : list) if (!set.add(value)) return false;
        return true;
    }
}
