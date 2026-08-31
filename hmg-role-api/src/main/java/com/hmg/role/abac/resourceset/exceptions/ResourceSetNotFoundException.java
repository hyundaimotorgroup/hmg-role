package com.hmg.role.abac.resourceset.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ResourceSetNotFoundException extends NotFoundException {

    private List<String> resourceSetKeys;

    public ResourceSetNotFoundException() {
        super("Resource Set Not Found");
    }

    public ResourceSetNotFoundException(List<String> resourceSetKeys) {
        this();
        this.resourceSetKeys = resourceSetKeys;
    }

    public ResourceSetNotFoundException(String... resourceSetKeys) {
        this(List.of(resourceSetKeys));
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Resource Set {0} Not Found", resourceSetKeys);
    }
}
