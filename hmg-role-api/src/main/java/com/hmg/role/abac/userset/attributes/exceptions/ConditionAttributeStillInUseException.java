package com.hmg.role.abac.userset.attributes.exceptions;

import com.hmg.role.abac.userset.attributes.dto.ConditionConflictDetailDto;
import com.hmg.role.util.exceptions.BeingUsedException;

public class ConditionAttributeStillInUseException extends BeingUsedException {

    public ConditionAttributeStillInUseException(ConditionConflictDetailDto detail) {
        super("Condition attribute is being used", detail);
    }

    public ConditionAttributeStillInUseException() {
        super("Condition attribute is being used", null);
    }
}
