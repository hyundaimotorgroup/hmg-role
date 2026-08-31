package com.hmg.role.rbac.policy.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ActionNotFoundException extends NotFoundException {

    private final List<String> actionNames;

    public ActionNotFoundException(String... actionNames) {
        this(List.of(actionNames));
    }

    public ActionNotFoundException(List<String> actionNames) {
        super("Action Not Found");
        this.actionNames = actionNames;
    }

    public String getLogMessage() {
        return MessageFormat.format("Action {0} Not Found", actionNames);
    }
}
