package com.hmg.role.sdk.db.keys;

import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.reader.model.PolicyItemCsvModel;
import java.io.Serializable;
import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ResourceActionMapDbKey
        implements ResourceTypeModel,
                ResourceActionModel,
                Comparable<ResourceActionMapDbKey>,
                Serializable {
    String resourceTypeKey;
    String actionName;

    public ResourceActionMapDbKey(PolicyItemCsvModel policyCsv) {
        this(policyCsv.getResourceTypeKey(), policyCsv.getActionName());
    }

    @Override
    public int compareTo(ResourceActionMapDbKey o) {
        return COMPARATOR.compare(this, o);
    }

    public static Comparator<ResourceActionMapDbKey> COMPARATOR =
            (o1, o2) -> {
                if (o1 != null && o2 != null) {
                    int res = StringUtils.compare(o1.resourceTypeKey, o2.resourceTypeKey);
                    if (res != 0) {
                        return res;
                    } else {
                        return StringUtils.compare(o1.actionName, o2.actionName);
                    }
                } else {
                    if (o1 == null && o2 == null) return 0; // both null
                    if (o1 == null) return -1; // o1 is null and o2 not
                    return 1; // o2 is null and o1 not
                }
            };
}
