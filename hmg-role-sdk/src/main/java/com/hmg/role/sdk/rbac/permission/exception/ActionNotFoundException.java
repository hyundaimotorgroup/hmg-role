package com.hmg.role.sdk.rbac.permission.exception;

import com.hmg.role.sdk.common.exception.NotFoundException;
import java.util.Collection;
import lombok.Getter;

@Getter
public class ActionNotFoundException extends NotFoundException {

    private final String resourceTypeKey;
    private final Collection<String> actionNames;

    public ActionNotFoundException(String resourceTypeKey, Collection<String> actionNames) {
        super(
                "Action not found for "
                        + actionNames
                        + " with resourceTypeKey '"
                        + resourceTypeKey
                        + "'");
        this.actionNames = actionNames;
        this.resourceTypeKey = resourceTypeKey;
    }
}
