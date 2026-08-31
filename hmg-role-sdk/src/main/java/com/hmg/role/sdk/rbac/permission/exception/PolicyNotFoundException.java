package com.hmg.role.sdk.rbac.permission.exception;

import com.hmg.role.sdk.common.exception.NotFoundException;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemKey;
import java.util.Collection;
import lombok.Getter;

@Getter
public class PolicyNotFoundException extends NotFoundException {

    private final Collection<PolicyItemKey> policyItemKeys;

    public PolicyNotFoundException(Collection<PolicyItemKey> policyItemKeys) {
        super("Policy not found for " + policyItemKeys);
        this.policyItemKeys = policyItemKeys;
    }
}
