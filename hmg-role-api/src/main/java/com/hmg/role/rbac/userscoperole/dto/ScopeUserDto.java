package com.hmg.role.rbac.userscoperole.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ScopeUserDto(
        @Schema(title = "user key") String userKey,
        @Schema(title = "user name") String userName,
        @Schema(title = "scope key") String scopeKey,
        @Schema(title = "scope name") String scopeName) {}
