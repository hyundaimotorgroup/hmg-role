package com.hmg.role.abac.userset.attributes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ConditionAttributeDeleteDto(@Schema @NotNull String key) {}
