package com.hmg.role.abac.common.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class AbacAttributeNullValueException extends BadRequestException {
    public AbacAttributeNullValueException(String attributeKey) {
        super("Attribute: %s can't be parsed".formatted(attributeKey));
    }
}
