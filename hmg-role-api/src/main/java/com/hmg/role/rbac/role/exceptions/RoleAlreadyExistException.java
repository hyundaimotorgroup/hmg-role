package com.hmg.role.rbac.role.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class RoleAlreadyExistException extends AlreadyExistException {

    private final List<String> roles;

    public RoleAlreadyExistException(String... role) {
        this(List.of(role));
    }

    public RoleAlreadyExistException(List<String> roles) {
        super("Role Already Exist");
        this.roles = roles;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Role {0} Already Exist", roles);
    }
}
