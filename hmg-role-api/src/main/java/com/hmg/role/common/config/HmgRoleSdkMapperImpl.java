package com.hmg.role.common.config;

import com.hmg.role.rbac.permission.dto.PermissionFlattenedResponseDto;
import com.hmg.role.rbac.permission.dto.PermissionRequestDto;
import com.hmg.role.rbac.policy.policyitem.PolicyItem;
import com.hmg.role.sdk.common.enums.Effect;
import com.hmg.role.sdk.rbac.permission.dto.*;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponse;
import com.hmg.role.sdk.rbac.permission.dto.PermissionResponse;
import com.hmg.role.sdk.rbac.permission.dto.ResourceRequestDto;
import com.hmg.role.sdk.rbac.permission.dto.UserSubjectRequest;
import com.hmg.role.sdk.rbac.permission.dto.UserSubjectRequestDto;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemModel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;

public class HmgRoleSdkMapperImpl implements HmgRoleSdkMapper {

    public PermissionFlattenedResponseDto permissionFlattenedResponseDto(
            PermissionFlatResponse sdkResp) {
        var apiResp = new PermissionFlattenedResponseDto();
        apiResp.setRole(sdkResp.getRoleKey());
        apiResp.setAction(sdkResp.getActionName());
        apiResp.setScope(sdkResp.getScopeKey());
        apiResp.setEffect(sdkResp.getEffect().name());
        apiResp.setType(sdkResp.getResourceTypeKey());
        return apiResp;
    }

    public com.hmg.role.rbac.permission.dto.PermissionResponseDto toPermissionResponseDto(
            PermissionResponse sdkResp) {

        var resourceRespDto =
                new com.hmg.role.rbac.permission.dto.PermissionResponseDto.ResourceResponseDto();
        resourceRespDto.setType(sdkResp.getResourceTypeKey());
        resourceRespDto.setScope(sdkResp.getScopeKey());

        List<com.hmg.role.rbac.permission.dto.PermissionResponseDto.ActionEffectDto> actionEffects =
                sdkResp.getActionEffects().stream()
                        .map(
                                actionEffectSdk -> {
                                    var actionEffectDto =
                                            new com.hmg.role.rbac.permission.dto
                                                    .PermissionResponseDto.ActionEffectDto();
                                    actionEffectDto.setEffect(
                                            com.hmg.role.rbac.policy.enums.Effect.valueOf(
                                                    actionEffectSdk.getEffect().name()));
                                    actionEffectDto.setRole(actionEffectSdk.getRoleKey());
                                    actionEffectDto.setAction(actionEffectSdk.getActionName());
                                    return actionEffectDto;
                                })
                        .toList();

        var dtoResp = new com.hmg.role.rbac.permission.dto.PermissionResponseDto();
        dtoResp.setResource(resourceRespDto);
        dtoResp.setActionEffects(actionEffects);

        return dtoResp;
    }

    @Override
    public UserSubjectRequest toSdkUserSubjectRequest(
            PermissionRequestDto.PermissionRequestUserDto apiUserDto) {
        return UserSubjectRequestDto.builder()
                .userKey(apiUserDto.key())
                .scopeKey(apiUserDto.scope())
                .build();
    }

    @Override
    public ResourceRequestDto toSdkResourceRequestDto(
            PermissionRequestDto.ResourceActionsDto apiReq) {
        return toSdkResourceRequestDto(apiReq.resource(), new HashSet<>(apiReq.actions()));
    }

    private ResourceRequestDto toSdkResourceRequestDto(
            PermissionRequestDto.ResourceActionsDto.ResourceRequestDto apiReq,
            Set<String> actions) {
        return ResourceRequestDto.builder()
                .resourceTypeKey(apiReq.type())
                .scopeKey(apiReq.scope())
                .actionNames(actions)
                .build();
    }

    public PolicyItemModel toSdkPolicyItemModel(PolicyItem pi) {
        return new PolicyItemModel() {

            @Getter private Effect effect = Effect.valueOf(pi.getEffect().name());

            @Getter private String roleKey = pi.getRole().getKey();

            @Getter private String scopeKey = pi.getScope().getKey();

            @Getter private String actionName = pi.getResourceAction().getActionName();

            @Getter
            private String resourceTypeKey = pi.getResourceAction().getResourceType().getKey();
        };
    }
}
