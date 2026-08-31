package com.hmg.role.abac.resourceset.exceptions;

import com.hmg.role.util.exceptions.BeingUsedException;

public class ResourceSetIsBeingUsedException extends BeingUsedException {
    public ResourceSetIsBeingUsedException() {
        super("Resource Set Is Being Used");
    }
}
