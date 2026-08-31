package com.hmg.role.rbac.user.dto;

import com.hmg.role.rbac.userscoperole.dto.CreateUserScopeRoleDto;
import com.hmg.role.util.Constants;
import com.hmg.role.util.enums.CharacterClass;
import com.hmg.role.util.validation.annotations.NoDuplicateValues;
import com.hmg.role.util.validation.annotations.ValidCharacters;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public record CreateUserDto(
        @Schema(title = "User key")
                @NotBlank
                @Size(max = Constants.MAX_50_SIZE)
                @ValidCharacters(
                        allowedCharacters = {
                            CharacterClass.LATIN_ALPHABET,
                            CharacterClass.ARABIC_NUMERAL,
                            CharacterClass.DASH,
                            CharacterClass.UNDERSCORE
                        })
                String key,
        @Schema(title = "User name") @Size(max = Constants.MAX_50_SIZE) String name,
        @Schema(title = "User metadata") @Size(max = Constants.MAX_10_SIZE)
                Map<String, String> metadata,
        @Schema(title = "Scope role") @NotNull @NoDuplicateValues
                List<@Valid CreateUserScopeRoleDto> scopeRoles) {}
