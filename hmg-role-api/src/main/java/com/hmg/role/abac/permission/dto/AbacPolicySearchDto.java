package com.hmg.role.abac.permission.dto;

import com.hmg.role.util.Constants;
import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbacPolicySearchDto extends PageRequestDto { // can't use CommonAbacSearchDto here
    @Parameter(description = "Resource Set Key, Prefix match", example = "frequently-ask-questions")
    @Schema
    @Size(max = Constants.MAX_255_SIZE)
    private String resourceSetKeyLike;

    @Parameter(description = "User Set Key. Prefix match", example = "it-dept")
    @Schema
    @Size(max = Constants.MAX_255_SIZE)
    private String userSetKeyLike;

    @Parameter(description = "Selected scope")
    @Schema
    @NotBlank
    @Size(max = Constants.MAX_255_SIZE)
    private String scopeKey;
}
