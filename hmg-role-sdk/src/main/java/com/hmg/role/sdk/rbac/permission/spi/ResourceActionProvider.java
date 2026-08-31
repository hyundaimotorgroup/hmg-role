package com.hmg.role.sdk.rbac.permission.spi;

import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionSetModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public interface ResourceActionProvider {

    @Nonnull
    Stream<ResourceActionModel> findActionsByTypeAndNames(
            @Nonnull ResourceTypeModel resourceType, @Nonnull ResourceActionSetModel actionNames);

    @Nonnull
    Stream<ResourceActionModel> findActionsByType(@Nonnull ResourceTypeModel resourceType);
}
