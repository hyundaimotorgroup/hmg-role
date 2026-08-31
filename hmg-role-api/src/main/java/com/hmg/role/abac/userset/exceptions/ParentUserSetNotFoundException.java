package com.hmg.role.abac.userset.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ParentUserSetNotFoundException extends NotFoundException {

    private final List<String> parentUserSet;

    public ParentUserSetNotFoundException(List<String> parentUserSet) {
        super("Parent User Set Not Found");
        this.parentUserSet = parentUserSet;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Parent User Set Not Found {0}", parentUserSet);
    }
}
