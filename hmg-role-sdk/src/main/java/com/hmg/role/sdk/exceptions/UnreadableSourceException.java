package com.hmg.role.sdk.exceptions;

public class UnreadableSourceException extends HmgRoleSdkException {
    public UnreadableSourceException() {
        super("unable to read given CSV file ");
    }
}
