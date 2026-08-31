package com.hmg.role.abac.userset.attributes.exceptions;

import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.exceptions.BadRequestException;

public class OperandTypeInvalidException extends BadRequestException { // NOSONAR
    // TODO maybe need to simplify request handling exceptions
    // TODO as currently `BadRequestException` has inheritance level of 5
    // TODO and sonar complains for anything > 5
    public OperandTypeInvalidException(String operand, OperandDataType type) {
        super("Invalid key dataType: " + operand + ", dataType: " + type);
    }
}
