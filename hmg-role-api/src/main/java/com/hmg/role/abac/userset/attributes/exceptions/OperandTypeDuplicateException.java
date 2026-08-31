package com.hmg.role.abac.userset.attributes.exceptions;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.util.exceptions.BadRequestException;

public class OperandTypeDuplicateException extends BadRequestException {
    public OperandTypeDuplicateException(String operandName, OperandSubject operandSubject) {
        super("Duplicate attribute for: %s in %s".formatted(operandName, operandSubject));
    }
}
