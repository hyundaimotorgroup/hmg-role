package com.hmg.role.rbac.role.projections;

public interface RoleProjection {

    String getName();

    String getKey();

    long getCountUsersPerRole();
}
