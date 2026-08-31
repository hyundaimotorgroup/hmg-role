package com.hmg.role.rbac.policy.dto;

import java.util.List;
import lombok.Data;

@Data
public class ResourceTypeWithPolicyActionsDto {
    private String key;
    private String name;
    private List<ActionWithEffectDto> actions;
    List<ResourceTypeWithPolicyActionsDto> children;
}
