package com.hmg.role.rbac.policy.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import lombok.Getter;

@Getter
public class PolicyNotFoundException extends NotFoundException {

    private final Collection<String> policyKeys;

    public PolicyNotFoundException(Collection<String> policyKeys) {
        super("Policy Not Found");
        this.policyKeys = policyKeys;
    }

    public PolicyNotFoundException(String... policyKeys) {
        this(List.of(policyKeys));
    }

    @Override
    public String getLogMessage() {
        return MessageFormat.format("Policy {0} Not Found", policyKeys);
    }
}
