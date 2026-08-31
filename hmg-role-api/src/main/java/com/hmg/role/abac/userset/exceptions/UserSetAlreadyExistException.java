package com.hmg.role.abac.userset.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class UserSetAlreadyExistException extends AlreadyExistException {

    private List<String> userSetKeys;

    public UserSetAlreadyExistException() {
        super("User Set Already Exist");
    }

    public UserSetAlreadyExistException(List<String> userSetKeys) {
        this();
        this.userSetKeys = userSetKeys;
    }

    public UserSetAlreadyExistException(String... userSetKeys) {
        this(List.of(userSetKeys));
    }

    public String getLogMessage() {
        return MessageFormat.format("User Set {0} Already Exist", userSetKeys);
    }
}
