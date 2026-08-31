package com.hmg.role.abac.common.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class AbacNullPermissionAttributeValue extends BadRequestException {
    public AbacNullPermissionAttributeValue(String subject) {
        super("Attributes for %s must not be null".formatted(subject));
    }
}
