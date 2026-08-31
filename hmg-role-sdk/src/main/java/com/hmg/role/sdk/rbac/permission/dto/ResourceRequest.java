package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.rbac.permission.model.ResourceActionSetModel;
import com.hmg.role.sdk.rbac.permission.model.ResourceTypeModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import java.util.Set;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public interface ResourceRequest extends ScopeModel, ResourceTypeModel, ResourceActionSetModel {

    @NotBlank
    String getResourceTypeKey();

    @Nullable
    String getScopeKey();

    @NotEmpty
    Set<@NotBlank String> getActionNames();
}
