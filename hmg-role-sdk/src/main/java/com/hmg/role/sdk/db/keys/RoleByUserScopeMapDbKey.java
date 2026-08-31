package com.hmg.role.sdk.db.keys;

import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.model.UserModel;
import com.hmg.role.sdk.reader.model.PolicyItemCsvModel;
import java.io.Serializable;
import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RoleByUserScopeMapDbKey implements Comparable<RoleByUserScopeMapDbKey>, Serializable {
    public final UserModel user;
    public final ScopeModel scope;

    public RoleByUserScopeMapDbKey(PolicyItemCsvModel policy) {
        this(policy, policy);
    }

    @Override
    public int compareTo(RoleByUserScopeMapDbKey o) {
        return COMPARATOR.compare(this, o);
    }

    public static Comparator<RoleByUserScopeMapDbKey> COMPARATOR =
            (o1, o2) -> {
                if (o1 != null && o2 != null) {
                    int res = StringUtils.compare(o1.user.getUserKey(), o2.user.getUserKey());
                    if (res != 0) {
                        return res;
                    } else {
                        return StringUtils.compare(o1.scope.getScopeKey(), o2.scope.getScopeKey());
                    }
                } else {
                    if (o1 == null && o2 == null) return 0; // both null
                    if (o1 == null) return -1; // o1 is null and o2 not
                    return 1; // o2 is null and o1 not
                }
            };
}
