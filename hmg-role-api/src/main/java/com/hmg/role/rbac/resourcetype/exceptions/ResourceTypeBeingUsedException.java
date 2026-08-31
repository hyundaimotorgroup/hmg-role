package com.hmg.role.rbac.resourcetype.exceptions;

import com.hmg.role.rbac.resourcetype.dto.ResourceTypeConflictDetailDto;
import com.hmg.role.util.exceptions.BeingUsedException;
import lombok.Getter;

@Getter
public class ResourceTypeBeingUsedException extends BeingUsedException {

    public ResourceTypeBeingUsedException(ResourceTypeConflictDetailDto dto) {
        super("Resource dataType is being used in Policy", dto);
    }
}
