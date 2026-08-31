package com.hmg.role.rbac.user.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class UserAlreadyExistException extends AlreadyExistException {

    private final List<String> userKeys;

    public UserAlreadyExistException(String... userKeys) {
        this(List.of(userKeys));
    }

    public UserAlreadyExistException(List<String> userKeys) {
        super("User Already Exist");
        this.userKeys = userKeys;
    }

    public String getLogMessage() {
        return MessageFormat.format("User {0} Already Exist", userKeys);
    }
}
