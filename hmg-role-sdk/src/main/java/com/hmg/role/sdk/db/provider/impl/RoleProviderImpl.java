package com.hmg.role.sdk.db.provider.impl;

import com.hmg.role.sdk.db.MapDbStore;
import com.hmg.role.sdk.db.interfaces.UpdateHook;
import com.hmg.role.sdk.db.keys.RoleByUserScopeMapDbKey;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.model.UserModel;
import com.hmg.role.sdk.rbac.permission.spi.RoleProvider;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.mapdb.HTreeMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public final class RoleProviderImpl implements RoleProvider {
    private final MapDbStore mapDbStore;
    private final UpdateHook updateHook;

    @Override
    public Stream<RoleModel> findRolesByUserAndScope(UserModel user, ScopeModel scope) {
        updateHook.callUpdateHook();
        RoleByUserScopeMapDbKey key = new RoleByUserScopeMapDbKey(user, scope);
        return mapDbStore
                .getRolesByUserScopeKeyMap()
                .subMap(key, true, key, true)
                .values()
                .stream();
    }

    @Override
    public Stream<RoleModel> findRolesByKeys(Collection<String> roleKeys) {
        updateHook.callUpdateHook();
        HTreeMap<String, RoleModel> rolesByKeyMap = mapDbStore.getRolesByKeyMap();
        return roleKeys.stream().filter(rolesByKeyMap::containsKey).map(rolesByKeyMap::get);
    }
}
