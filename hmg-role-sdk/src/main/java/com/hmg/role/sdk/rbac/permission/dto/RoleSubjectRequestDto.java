package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.rbac.permission.model.RoleSetModel;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleSubjectRequestDto implements RoleSubjectRequest, RoleSetModel {

    @NotEmpty private Set<@NotBlank String> roleKeys;
}
