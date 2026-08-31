package com.hmg.role.rbac.user.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class UserNotFoundException extends NotFoundException {

    private final List<String> userKeys;

    public UserNotFoundException(String... userKeys) {
        this(List.of(userKeys));
    }

    public UserNotFoundException(List<String> userKeys) {
        super("User Not Found");
        this.userKeys = userKeys;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("User {0} Not Found", userKeys);
    }
}
