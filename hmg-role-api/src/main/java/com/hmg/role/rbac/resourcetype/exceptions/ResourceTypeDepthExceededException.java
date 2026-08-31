package com.hmg.role.rbac.resourcetype.exceptions;

import com.hmg.role.util.exceptions.BadRequestException;

public class ResourceTypeDepthExceededException extends BadRequestException {
    public ResourceTypeDepthExceededException(int wouldBeDepth, int maxDepth) {
        super(
                "Resource type would be at depth "
                        + wouldBeDepth
                        + ", which exceeds the maximum inheritance depth of "
                        + maxDepth);
    }
}
