package com.hmg.role.common.keymanagement;

import com.hmg.role.admin.project.Project;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;

public interface ProjectEncryptionKeyService {
    ProjectEncryptionKeyDto getAccessKey();

    ProjectEncryptionKeyDto create(Project project);

    void delete(String projectKey);
}
