package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.rbac.permission.model.UserModel;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

public interface UserSubjectRequest extends ScopeModel, UserModel {

    @NotBlank
    String getUserKey();

    @Nullable
    String getScopeKey();
}
