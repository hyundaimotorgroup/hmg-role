package com.hmg.role.api.admin;

import com.hmg.role.common.keymanagement.ProjectEncryptionKeyService;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/access-keys")
@RestController
@ConditionalOnExpression("T(java.lang.Boolean).valueOf('${cdc.enabled:false}')")
public class AccessKeyController {
    private final ProjectEncryptionKeyService projectEncryptionKeyService;

    @GetMapping
    public ProjectEncryptionKeyDto getSecrets() {
        return projectEncryptionKeyService.getAccessKey();
    }
}
