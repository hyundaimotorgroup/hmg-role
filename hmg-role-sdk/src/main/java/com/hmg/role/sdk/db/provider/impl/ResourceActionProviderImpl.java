package com.hmg.role.sdk.db.provider.impl;

import com.hmg.role.sdk.db.MapDbStore;
import com.hmg.role.sdk.db.interfaces.UpdateHook;
import com.hmg.role.sdk.db.keys.ResourceActionMapDbKey;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionSetModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.spi.ResourceActionProvider;
import java.util.NavigableMap;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.mapdb.BTreeMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceActionProviderImpl implements ResourceActionProvider {
    private final BTreeMap<ResourceActionMapDbKey, ResourceActionModel> resourceActionMap;
    private final UpdateHook updateHook;

    @Builder
    public ResourceActionProviderImpl(MapDbStore mapDbStore, UpdateHook updateHook) {
        this.resourceActionMap = mapDbStore.getResourceActionMap();
        this.updateHook = updateHook;
    }

    @Override
    public Stream<ResourceActionModel> findActionsByTypeAndNames(
            ResourceTypeModel resourceType, ResourceActionSetModel actionNames) {
        updateHook.callUpdateHook();

        return actionNames.getActionNames().stream()
                .flatMap(
                        k -> {
                            ResourceActionMapDbKey key =
                                    new ResourceActionMapDbKey(
                                            resourceType.getResourceTypeKey(), k);
                            return resourceActionMap.subMap(key, true, key, true).values().stream();
                        });
    }

    @Override
    public Stream<ResourceActionModel> findActionsByType(ResourceTypeModel resourceType) {
        updateHook.callUpdateHook();

        ResourceActionMapDbKey byType =
                new ResourceActionMapDbKey(resourceType.getResourceTypeKey(), null);
        NavigableMap<ResourceActionMapDbKey, ResourceActionModel> map =
                resourceActionMap.subMap(byType, true, byType, true);
        return map.values().stream();
    }
}
