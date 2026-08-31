package com.hmg.role.sdk.rbac.permission.dto;

import java.util.Set;
import javax.annotation.Nullable;
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
public class ResourceRequestDto implements ResourceRequest {

    @NotBlank private String resourceTypeKey;

    @Nullable private String scopeKey;

    @NotEmpty private Set<@NotBlank String> actionNames;
}
