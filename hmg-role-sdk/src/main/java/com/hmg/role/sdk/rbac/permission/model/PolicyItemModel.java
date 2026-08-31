package com.hmg.role.sdk.rbac.permission.model;

import com.hmg.role.sdk.common.enums.Effect;

public interface PolicyItemModel extends PolicyItemKey {

    Effect getEffect();
}
