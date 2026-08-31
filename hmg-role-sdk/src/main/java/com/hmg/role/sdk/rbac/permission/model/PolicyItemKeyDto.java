package com.hmg.role.sdk.rbac.permission.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PolicyItemKeyDto implements PolicyItemKey {
    private final String resourceTypeKey, actionName, scopeKey, roleKey;
}
