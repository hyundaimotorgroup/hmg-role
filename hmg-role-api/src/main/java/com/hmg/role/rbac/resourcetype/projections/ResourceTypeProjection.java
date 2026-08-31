package com.hmg.role.rbac.resourcetype.projections;

public interface ResourceTypeProjection {

    String getDescription();

    String getKey();

    String getActionName();

    String getName();

    String getTag();

    Long getParentId();
}
