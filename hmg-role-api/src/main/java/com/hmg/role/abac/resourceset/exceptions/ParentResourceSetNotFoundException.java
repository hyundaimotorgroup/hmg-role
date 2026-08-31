package com.hmg.role.abac.resourceset.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ParentResourceSetNotFoundException extends NotFoundException {

    private final List<String> parentResourceSet;

    public ParentResourceSetNotFoundException(List<String> parentResourceSet) {
        super("Parent Resource Set Not Found");
        this.parentResourceSet = parentResourceSet;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Parent Resource Set Not Found {0}", parentResourceSet);
    }
}
