package com.hmg.role.sdk.db.keys;

import com.hmg.role.sdk.rbac.permission.model.PolicyItemKey;
import java.io.Serializable;
import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.Strings;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class PolicyItemMapDbKey
        implements PolicyItemKey, Serializable, Comparable<PolicyItemMapDbKey> {
    public final String scopeKey;
    public final String roleKey;
    public final String resourceTypeKey;
    public final String actionName;

    public PolicyItemMapDbKey(PolicyItemKey k) {
        this.scopeKey = k.getScopeKey();
        this.roleKey = k.getRoleKey();
        this.resourceTypeKey = k.getResourceTypeKey();
        this.actionName = k.getActionName();
    }

    @Override
    public int compareTo(PolicyItemMapDbKey o) {
        return COMPARATOR.compare(this, o);
    }

    public static Comparator<PolicyItemMapDbKey> COMPARATOR =
            (first, second) -> {
                if (first != null && second != null) {
                    int res = Strings.CS.compare(first.scopeKey, second.scopeKey);
                    if (res != 0) {
                        return res;
                    }
                    res = Strings.CS.compare(first.roleKey, second.roleKey);
                    if (res != 0) {
                        return res;
                    }
                    res = Strings.CS.compare(first.resourceTypeKey, second.resourceTypeKey);
                    if (res != 0) {
                        return res;
                    }
                    return Strings.CS.compare(first.actionName, second.actionName);
                } else {
                    if (first == null && second == null) return 0; // both null
                    if (first == null) return -1; // first is null and second not
                    return 1; // first is null and second not
                }
            };
}
