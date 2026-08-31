package com.hmg.role.rbac.policy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Effect {
    ALLOW,

    DENY
}
