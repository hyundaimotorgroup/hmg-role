package com.hmg.role.rbac.policy.projections;

import java.util.List;

public interface PolicyWithItemKeysProjection {
    String getPolicyKey();

    List<String> getPolicyItemKeys();
}
