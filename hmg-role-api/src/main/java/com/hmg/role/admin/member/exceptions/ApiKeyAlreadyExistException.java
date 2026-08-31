package com.hmg.role.admin.member.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ApiKeyAlreadyExistException extends AlreadyExistException {

    private final List<String> apiKeys;

    public ApiKeyAlreadyExistException(String... apiKeys) {
        this(List.of(apiKeys));
    }

    public ApiKeyAlreadyExistException(List<String> apiKeys) {
        super("API Key Already Exist");
        this.apiKeys = apiKeys;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("API Key {0} Already Exist", apiKeys);
    }
}
