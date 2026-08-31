package com.hmg.role.rbac.permission.dto;

import com.hmg.role.rbac.policy.enums.Effect;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDto {

    @Schema private ResourceResponseDto resource;

    @Schema private List<ActionEffectDto> actionEffects;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionEffectDto {

        @Schema private String role;

        @Schema private String action;

        @Schema private Effect effect;
    }

    @Data
    public static class ResourceResponseDto {

        @Schema private String type;

        @Schema private String scope;
    }
}
