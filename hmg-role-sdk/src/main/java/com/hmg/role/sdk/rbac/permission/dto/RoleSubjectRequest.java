package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.rbac.permission.model.RoleSetModel;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public interface RoleSubjectRequest extends RoleSetModel {

    @NotEmpty
    Set<@NotBlank String> getRoleKeys();
}
