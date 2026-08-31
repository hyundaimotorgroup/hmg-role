package com.hmg.role.sdk.rbac.permission.spi;

import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public interface ScopeProvider {

    @Nonnull
    ScopeModel getDefaultScopeRbac();

    @Nonnull
    Stream<ScopeModel> findScopesByKeys(@Nonnull Collection<String> scopeKeys);
}
