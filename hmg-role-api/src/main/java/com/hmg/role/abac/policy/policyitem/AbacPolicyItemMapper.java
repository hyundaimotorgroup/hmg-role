package com.hmg.role.abac.policy.policyitem;

import com.hmg.role.abac.policy.AbacPolicy;
import com.hmg.role.abac.policy.AbacPolicyItem;
import com.hmg.role.abac.policy.dto.AbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateAbacPolicyDto;
import com.hmg.role.abac.policy.dto.UpdateBulkAbacPolicyDto;
import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.abac.userset.UserSet;
import com.hmg.role.common.config.CommonMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class AbacPolicyItemMapper {

    @Mapping(source = "abacPolicyDto.effect", target = "effect")
    @Mapping(source = "scope", target = "scope")
    @Mapping(source = "userSet", target = "userSet")
    @Mapping(source = "resourceSetAction", target = "resourceSetAction")
    @Mapping(source = "policy", target = "policy")
    @BeanMapping(ignoreByDefault = true)
    public abstract AbacPolicyItem toPolicyItem(
            AbacPolicyDto abacPolicyDto,
            ResourceSetAction resourceSetAction,
            AbacScope scope,
            AbacPolicy policy,
            UserSet userSet);

    @Mapping(source = "updateAbacPolicyDto.effect", target = "effect")
    @Mapping(source = "scope", target = "scope")
    @Mapping(source = "userSet", target = "userSet")
    @Mapping(source = "resourceSetAction", target = "resourceSetAction")
    @Mapping(source = "policy", target = "policy")
    @BeanMapping(ignoreByDefault = true)
    public abstract AbacPolicyItem toPolicyItem(
            UpdateAbacPolicyDto updateAbacPolicyDto,
            ResourceSetAction resourceSetAction,
            AbacScope scope,
            AbacPolicy policy,
            UserSet userSet);

    @Mapping(source = "updateBulkAbacPolicyDto.effect", target = "effect")
    @Mapping(source = "scope", target = "scope")
    @Mapping(source = "userSet", target = "userSet")
    @Mapping(source = "resourceSetAction", target = "resourceSetAction")
    @Mapping(source = "policy", target = "policy")
    @BeanMapping(ignoreByDefault = true)
    public abstract AbacPolicyItem toPolicyItem(
            UpdateBulkAbacPolicyDto updateBulkAbacPolicyDto,
            ResourceSetAction resourceSetAction,
            AbacScope scope,
            AbacPolicy policy,
            UserSet userSet);
}
