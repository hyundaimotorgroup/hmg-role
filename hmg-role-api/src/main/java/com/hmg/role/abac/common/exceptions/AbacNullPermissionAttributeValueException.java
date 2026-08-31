package com.hmg.role.abac.common.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class AbacNullPermissionAttributeValueException extends BadRequestException {
    public AbacNullPermissionAttributeValueException(String operand) {
        super("Given value for attribute: %s is null".formatted(operand));
    }
}
