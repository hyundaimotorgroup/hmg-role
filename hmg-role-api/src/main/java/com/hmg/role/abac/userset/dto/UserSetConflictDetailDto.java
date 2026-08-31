package com.hmg.role.abac.userset.dto;

import java.util.Collection;
import java.util.Set;

public record UserSetConflictDetailDto(
        Collection<UserSetConflictWithPolicyDto> userSets, Set<String> policyKeys) {}
