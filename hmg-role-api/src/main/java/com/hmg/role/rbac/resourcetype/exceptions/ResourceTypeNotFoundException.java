package com.hmg.role.rbac.resourcetype.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ResourceTypeNotFoundException extends NotFoundException {

    private final List<String> resourceTypeKeys;

    public ResourceTypeNotFoundException(String... resourceTypeKeys) {
        this(List.of(resourceTypeKeys));
    }

    public ResourceTypeNotFoundException(List<String> resourceTypeKeys) {
        super("Resource Type Not Found");
        this.resourceTypeKeys = resourceTypeKeys;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Resource Type {0} Not Found", resourceTypeKeys);
    }
}
