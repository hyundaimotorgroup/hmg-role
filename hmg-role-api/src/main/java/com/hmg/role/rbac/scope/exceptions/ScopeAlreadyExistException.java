package com.hmg.role.rbac.scope.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ScopeAlreadyExistException extends AlreadyExistException {

    private final List<String> scopes;
    private final String conflictType;

    public ScopeAlreadyExistException(String... scope) {
        this(List.of(scope), null);
    }

    public ScopeAlreadyExistException(String scope, String conflictType) {
        this(List.of(scope), conflictType);
    }

    public ScopeAlreadyExistException(List<String> scopes, String conflictType) {
        super(buildMessage(conflictType));
        this.scopes = scopes;
        this.conflictType = conflictType;
    }

    private static String buildMessage(String conflictType) {
        if ("KEY".equals(conflictType)) {
            return "Scope key already exists";
        } else if ("NAME".equals(conflictType)) {
            return "Scope name already exists";
        } else if ("BOTH".equals(conflictType)) {
            return "Scope key and name already exist";
        } else {
            return "Scope already exists";
        }
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Scope {0} Already Exist", scopes);
    }
}
