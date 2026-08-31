package com.hmg.role.admin.audit.dto;

import com.hmg.role.util.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AuditTrailsRequestDto extends PageRequestDto {
    @Parameter(description = "Audit trail starting date", required = true)
    String startDate;

    @Parameter(description = "Audit trail end date", required = true)
    String endDate;

    @Parameter(
            in = ParameterIn.QUERY,
            description = "Select project key",
            schema = @Schema(type = "string"))
    String projectKey;

    @Parameter(description = "Select scope key", schema = @Schema(type = "string"))
    String scopeKey;

    @Parameter(description = "Select entity path to filter", schema = @Schema(type = "string"))
    String entityPath;

    @Parameter(description = "Entity key filter", schema = @Schema(type = "string"))
    String key;

    @Parameter(description = "Author filter of the change", schema = @Schema(type = "string"))
    private String author;

    @Parameter(description = "Client IP filter of the change", schema = @Schema(type = "string"))
    private String ip;
}
