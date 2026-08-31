package com.hmg.role.abac.common.exceptions;

import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.exceptions.BadRequestException;

public class AbacAttributeInvalidTypeException extends BadRequestException {
    public AbacAttributeInvalidTypeException(String attributeKey, OperandDataType expectedType) {
        super(
                "Attribute: %s is declared as %s but its inside quotes. %s values shouldn't be quoted"
                        .formatted(attributeKey, expectedType.name(), expectedType.name()));
    }
}
