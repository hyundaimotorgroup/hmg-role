package com.hmg.role.abac.userset.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class UserSetNotFoundException extends NotFoundException {

    private List<String> userSets;

    public UserSetNotFoundException() {
        super("User Set Not Found");
    }

    public UserSetNotFoundException(List<String> userSets) {
        super(String.format("User Set Not Found: %s", String.join(", ", userSets)));
        this.userSets = userSets;
    }

    public UserSetNotFoundException(String... userSets) {
        this(List.of(userSets));
    }

    public String getLogMessage() {
        return MessageFormat.format("User Set {0} Not Found", userSets);
    }
}
