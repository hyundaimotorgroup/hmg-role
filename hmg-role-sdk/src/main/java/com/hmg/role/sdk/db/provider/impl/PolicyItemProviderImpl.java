package com.hmg.role.sdk.db.provider.impl;

import com.hmg.role.sdk.db.MapDbStore;
import com.hmg.role.sdk.db.interfaces.UpdateHook;
import com.hmg.role.sdk.db.keys.PolicyItemMapDbKey;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemKey;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemModel;
import com.hmg.role.sdk.rbac.permission.spi.PolicyItemProvider;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public final class PolicyItemProviderImpl implements PolicyItemProvider {
    private final MapDbStore mapDbStore;
    private final UpdateHook updateHook;

    @Override
    public Stream<PolicyItemModel> findAllPoliciesByKeys(
            Collection<? extends PolicyItemKey> policyItemKeys) {
        updateHook.callUpdateHook();
        return policyItemKeys.stream()
                .map(PolicyItemMapDbKey::new)
                .filter(mapDbStore.getPolicyItemByKeyMap()::containsKey)
                .map(mapDbStore.getPolicyItemByKeyMap()::get);
    }
}
