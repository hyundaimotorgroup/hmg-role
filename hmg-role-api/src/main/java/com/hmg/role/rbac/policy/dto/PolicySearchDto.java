package com.hmg.role.rbac.policy.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class PolicySearchDto extends PageRequestDto {

    @Parameter public String resourceType;

    @Parameter private String roleKey;

    @Parameter private String scopeKey;

    @Parameter private String action;
}
