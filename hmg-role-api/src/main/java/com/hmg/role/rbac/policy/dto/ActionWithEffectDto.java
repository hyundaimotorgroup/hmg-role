package com.hmg.role.rbac.policy.dto;

import lombok.Data;

@Data
public class ActionWithEffectDto {
    private String action;
    private boolean allowed;
}
