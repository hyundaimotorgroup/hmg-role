package com.hmg.role.rbac.role.dto;

import java.util.Collection;
import java.util.Set;
import lombok.Builder;

@Builder
public record RoleConflictDetailDto(
        Collection<UserConflictWithRoleDto> users, Set<String> policyKeys) {}
