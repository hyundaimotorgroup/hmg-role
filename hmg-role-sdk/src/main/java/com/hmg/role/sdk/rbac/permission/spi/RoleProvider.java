package com.hmg.role.sdk.rbac.permission.spi;

import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.model.UserModel;
import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public interface RoleProvider {

    @Nonnull
    Stream<RoleModel> findRolesByUserAndScope(@Nonnull UserModel user, @Nonnull ScopeModel scope);

    @Nonnull
    Stream<RoleModel> findRolesByKeys(@Nonnull Collection<String> roleKeys);
}
