package com.hmg.role.sdk.db.provider.impl;

import com.hmg.role.sdk.common.SdkConstants;
import com.hmg.role.sdk.db.MapDbStore;
import com.hmg.role.sdk.db.interfaces.UpdateHook;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.spi.ScopeProvider;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.mapdb.HTreeMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScopeProviderImpl implements ScopeProvider {
    private final HTreeMap<String, ScopeModel> scopeByKeyMap;
    private final UpdateHook updateHook;

    @Builder
    public ScopeProviderImpl(MapDbStore mapDbStore, UpdateHook updateHook) {
        this.scopeByKeyMap = mapDbStore.getScopeByKeyMap();
        this.updateHook = updateHook;
    }

    @Override
    public Stream<ScopeModel> findScopesByKeys(Collection<String> scopeKeys) {
        return scopeKeys.stream().filter(scopeByKeyMap::containsKey).map(scopeByKeyMap::get);
    }

    @Override
    public ScopeModel getDefaultScopeRbac() {
        return scopeByKeyMap.get(SdkConstants.DEFAULT_SCOPE_KEY);
    }
}
