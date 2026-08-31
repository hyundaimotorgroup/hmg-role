package com.hmg.role.sdk.rbac.permission.spi;

import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public interface ResourceTypeProvider {

    @Nonnull
    Stream<ResourceTypeModel> findTypesByKeys(@Nonnull Collection<String> resourceTypeKeys);
}
