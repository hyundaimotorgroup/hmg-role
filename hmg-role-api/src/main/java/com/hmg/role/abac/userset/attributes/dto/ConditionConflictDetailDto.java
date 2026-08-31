package com.hmg.role.abac.userset.attributes.dto;

import java.util.Collection;

public record ConditionConflictDetailDto(Collection<ConditionConflictDto> conflictSource) {}
