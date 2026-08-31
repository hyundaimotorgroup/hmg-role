package com.hmg.role.rbac.policy.dto;

import com.hmg.role.rbac.policy.enums.Effect;
import java.util.List;

public interface IPolicyModificationDto {

    String key(); // policyKey

    String description();

    String resourceType();

    List<String> actions();

    String scope();

    List<String> roles();

    Effect effect();
}
