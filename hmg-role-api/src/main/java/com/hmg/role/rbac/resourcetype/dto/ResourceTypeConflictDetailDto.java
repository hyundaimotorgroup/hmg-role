package com.hmg.role.rbac.resourcetype.dto;

import java.util.Collection;
import lombok.Builder;

@Builder
public record ResourceTypeConflictDetailDto(
        Collection<String> projectKeys,
        Collection<String> resourceTypeKeys,
        Collection<String> policyKeys) {}
