package com.hmg.role.rbac.policy.policyitem;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class DuplicatePolicyItemException extends AlreadyExistException {

    private final List<PolicyItem> policyItems;

    public DuplicatePolicyItemException(List<PolicyItem> policyItems) {
        super("Duplicate Policy Item");
        this.policyItems = policyItems;
    }

    public String getLogMessage() {
        var ids = policyItems.stream().map(policyItem -> policyItem.getId()).toList();
        return MessageFormat.format("Duplicate Policy Item ids: {0}", ids);
    }
}
