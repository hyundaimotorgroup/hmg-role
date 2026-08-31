package com.hmg.role.sdk.common.enums;

public enum Effect {
    ALLOW,
    DENY;

    public boolean isAllowed() {
        return this == ALLOW;
    }

    public boolean isDenied() {
        return this == DENY;
    }
}
