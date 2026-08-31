package com.hmg.role.sdk.rbac.permission.dto;

import com.hmg.role.sdk.common.enums.Effect;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PermissionResponseDto implements PermissionResponse {

    private String resourceId;
    private String resourceTypeKey;
    private String scopeKey;
    private List<ActionEffectDto> actionEffects;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ActionEffectDto implements PermissionResponse.ActionEffect {

        private String roleKey;
        private String actionName;
        private Effect effect;
    }
}
