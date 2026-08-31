package com.hmg.role.abac.resourceset.exceptions;

import com.hmg.role.util.enums.OperandPosition;
import com.hmg.role.util.exceptions.BadRequestException;

public class NullResourceSetOperandException extends BadRequestException {
    public NullResourceSetOperandException(OperandPosition position) {
        super("ABAC resource set %s operand is null".formatted(position));
    }
}
