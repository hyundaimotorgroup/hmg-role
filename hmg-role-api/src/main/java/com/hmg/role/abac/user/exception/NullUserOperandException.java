package com.hmg.role.abac.user.exception;

import com.hmg.role.util.enums.OperandPosition;
import com.hmg.role.util.exceptions.BadRequestException;

public class NullUserOperandException extends BadRequestException {
    public NullUserOperandException(OperandPosition position) {
        super("ABAC user %s operand is null".formatted(position));
    }
}
