package com.hmg.role.rbac.role.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class RoleNotFoundException extends NotFoundException {

    private final List<String> roles;

    public RoleNotFoundException(String... roles) {
        this(List.of(roles));
    }

    public RoleNotFoundException(List<String> roles) {
        super("Role Not Found");
        this.roles = roles;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Role {0} Not Found", roles);
    }
}
