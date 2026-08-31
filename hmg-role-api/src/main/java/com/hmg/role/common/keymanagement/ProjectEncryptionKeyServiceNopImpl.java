package com.hmg.role.common.keymanagement;

import com.hmg.role.admin.project.Project;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("!T(java.lang.Boolean).valueOf('${cdc.enabled:false}')")
public class ProjectEncryptionKeyServiceNopImpl implements ProjectEncryptionKeyService {
    // NOP-implementation when CDC is disabled

    @Override
    public ProjectEncryptionKeyDto getAccessKey() {
        return null;
    }

    @Override
    public ProjectEncryptionKeyDto create(Project project) {
        return null;
    }

    @Override
    public void delete(String projectKey) {}
}
