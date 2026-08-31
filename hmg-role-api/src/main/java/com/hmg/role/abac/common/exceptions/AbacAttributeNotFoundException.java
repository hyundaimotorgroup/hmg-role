package com.hmg.role.abac.common.exceptions;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.util.exceptions.BadRequestException;

public class AbacAttributeNotFoundException extends BadRequestException {
    public AbacAttributeNotFoundException(
            OperandSubject subject, String attributeKey, String projectKey) {
        super(
                "No %s attribute with key: %s in project: %s"
                        .formatted(subject, attributeKey, projectKey));
    }
}
