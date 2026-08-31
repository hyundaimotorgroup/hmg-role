package com.hmg.role.abac.resourceset.exceptions;

import com.hmg.role.util.Constants;
import com.hmg.role.util.exceptions.BadRequestException;

public class ResourceSetTooManyException extends BadRequestException {
    public ResourceSetTooManyException() {
        super(
                "Too many resource sets in the project. Total resource count should not exceed %d"
                        .formatted(Constants.MAX_500_SIZE));
    }

    public ResourceSetTooManyException(int existingResourceSetCount, int newResourceSetCount) {
        super(
                "Too many resource sets in the project. Existing count: %d, new count: %d. Total resource count should not exceed %d"
                        .formatted(
                                existingResourceSetCount,
                                newResourceSetCount,
                                Constants.MAX_500_SIZE));
    }
}
