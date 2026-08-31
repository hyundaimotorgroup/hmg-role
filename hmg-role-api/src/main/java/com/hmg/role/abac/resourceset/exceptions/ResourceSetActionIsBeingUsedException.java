package com.hmg.role.abac.resourceset.exceptions;

import com.hmg.role.util.exceptions.BeingUsedException;

public class ResourceSetActionIsBeingUsedException extends BeingUsedException {
    public ResourceSetActionIsBeingUsedException() {
        super("Resource Set Action is being used in a Policy");
    }
}
