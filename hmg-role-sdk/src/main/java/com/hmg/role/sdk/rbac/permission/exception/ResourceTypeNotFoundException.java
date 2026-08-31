package com.hmg.role.sdk.rbac.permission.exception;

import com.hmg.role.sdk.common.exception.NotFoundException;
import java.util.Collection;
import lombok.Getter;

@Getter
public class ResourceTypeNotFoundException extends NotFoundException {

    private final Collection<String> resourceTypeKeys;

    public ResourceTypeNotFoundException(Collection<String> resourceTypeKeys) {
        super("ResourceType not found for key " + resourceTypeKeys);
        this.resourceTypeKeys = resourceTypeKeys;
    }
}
