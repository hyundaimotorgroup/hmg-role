package com.hmg.role.sdk.rbac.permission.spi;

import com.hmg.role.sdk.rbac.permission.model.PolicyItemKey;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemModel;
import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public interface PolicyItemProvider {

    @Nonnull
    Stream<PolicyItemModel> findAllPoliciesByKeys(
            @Nonnull Collection<? extends PolicyItemKey> policyItemKeys);
}
