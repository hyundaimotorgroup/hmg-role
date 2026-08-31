package com.hmg.role.rbac.resourcetype.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ResourceTypeAlreadyExistException extends AlreadyExistException {

    private final List<String> resourceTypeKeys;

    public ResourceTypeAlreadyExistException(String... resourceTypeKey) {
        this(List.of(resourceTypeKey));
    }

    public ResourceTypeAlreadyExistException(List<String> resourceTypeKeys) {
        super("Resource Type Already Exist");
        this.resourceTypeKeys = resourceTypeKeys;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Resource Type {0} Already Exist", resourceTypeKeys);
    }
}
