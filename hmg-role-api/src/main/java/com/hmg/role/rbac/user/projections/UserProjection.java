package com.hmg.role.rbac.user.projections;

import java.util.List;

public interface UserProjection {

    String getKey();

    String getName();

    List<String> getRoles();

    String getScope();
}
