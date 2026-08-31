package com.hmg.role.abac.userset.exceptions;

import com.hmg.role.abac.userset.dto.UserSetConflictDetailDto;
import com.hmg.role.util.exceptions.BeingUsedException;

public class UserSetIsBeingUsedException extends BeingUsedException {

    public UserSetIsBeingUsedException(UserSetConflictDetailDto detail) {
        super("User Set is being used", detail);
    }

    public UserSetIsBeingUsedException() {
        super("User Set is being used", null);
    }
}
