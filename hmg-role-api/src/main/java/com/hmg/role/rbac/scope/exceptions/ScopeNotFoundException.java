package com.hmg.role.rbac.scope.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ScopeNotFoundException extends NotFoundException {

    private final List<String> scopeKeys;

    public ScopeNotFoundException(String... scopeKeys) {
        this(List.of(scopeKeys));
    }

    public ScopeNotFoundException(List<String> resourceTypeKeys) {
        super("Scope Not Found");
        this.scopeKeys = resourceTypeKeys;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Scope {0} Not Found", scopeKeys);
    }
}
