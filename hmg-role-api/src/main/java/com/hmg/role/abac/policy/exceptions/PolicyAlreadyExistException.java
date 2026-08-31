package com.hmg.role.abac.policy.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class PolicyAlreadyExistException extends AlreadyExistException {

    private List<String> policyKeys;

    public PolicyAlreadyExistException() {
        super("Policy Already Exist");
    }

    public PolicyAlreadyExistException(List<String> policyKeys) {
        this();
        this.policyKeys = policyKeys;
    }

    public PolicyAlreadyExistException(String... policyKeys) {
        this(List.of(policyKeys));
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Policy {0} Already Exist", policyKeys);
    }
}
