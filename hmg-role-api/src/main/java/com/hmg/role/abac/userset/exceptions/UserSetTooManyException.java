package com.hmg.role.abac.userset.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class UserSetTooManyException extends BadRequestException {
    public UserSetTooManyException() {
        super("Too many user sets in the project. Total user count should not exceed 100");
    }

    public UserSetTooManyException(int existingCount, int newCount, int maxCount) {
        super(
                "Too many user sets in the project. Existing count: %d, new count: %d. New total would be %d which exceeds maximum of %d"
                        .formatted(existingCount, newCount, existingCount + newCount, maxCount));
    }
}
