package com.hmg.role.rbac.scope.dto;

import java.util.Set;
import lombok.Builder;

@Builder
public record ScopeConflictDetailDto(
        Set<String> projectKeys, Set<String> userKeys, Set<String> policyKeys) {}
