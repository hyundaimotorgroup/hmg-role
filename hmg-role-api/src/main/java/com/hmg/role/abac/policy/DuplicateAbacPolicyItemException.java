package com.hmg.role.abac.policy;

import com.hmg.role.util.exceptions.BeingUsedException;

public class DuplicateAbacPolicyItemException extends BeingUsedException {
    public DuplicateAbacPolicyItemException() {
        super("Policy item combination already exists in another policy");
    }
}
