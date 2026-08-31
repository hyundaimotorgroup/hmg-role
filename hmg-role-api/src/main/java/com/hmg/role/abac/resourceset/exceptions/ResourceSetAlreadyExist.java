package com.hmg.role.abac.resourceset.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ResourceSetAlreadyExist extends AlreadyExistException {

    private List<String> resourceSetKeys;

    public ResourceSetAlreadyExist() {
        super("Resource Set Already Exist");
    }

    public ResourceSetAlreadyExist(List<String> resourceSetKeys) {
        this();
        this.resourceSetKeys = resourceSetKeys;
    }

    public ResourceSetAlreadyExist(String... resourceSetKeys) {
        this(List.of(resourceSetKeys));
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Resource Set {0} Already Exist", resourceSetKeys);
    }
}
