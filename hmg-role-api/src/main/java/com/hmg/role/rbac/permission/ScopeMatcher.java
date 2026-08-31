package com.hmg.role.rbac.permission;

import org.springframework.stereotype.Component;

@Deprecated
@Component
public class ScopeMatcher {

    public boolean isScopeAllowed(String scopeA, String scopeB) {
        // 1. Handle exact match
        if (scopeA.equals(scopeB)) {
            return true;
        }
        // 2. Handle hierarchical match (scopeA is an ancestor of scopeB)
        return scopeB.startsWith(scopeA + ".");
    }
}
