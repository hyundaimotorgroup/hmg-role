package com.hmg.role.rbac.policy.projections;

public interface PolicyProjection {

    String getKey();

    String getDescription();

    String getScopeKey();

    String getResourceType();

    String getActions();

    String getRoleKey();

    String getEffect();
}
