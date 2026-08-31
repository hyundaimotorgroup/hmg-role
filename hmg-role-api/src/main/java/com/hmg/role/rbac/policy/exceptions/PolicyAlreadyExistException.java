package com.hmg.role.rbac.policy.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class PolicyAlreadyExistException extends AlreadyExistException {

    private final List<String> policyKeys;

    public PolicyAlreadyExistException(String... policyKey) {
        this(List.of(policyKey));
    }

    public PolicyAlreadyExistException(List<String> policyKeys) {
        super("Policy Already Exist");
        this.policyKeys = policyKeys;
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Policy {0} Already Exist", policyKeys);
    }
}
