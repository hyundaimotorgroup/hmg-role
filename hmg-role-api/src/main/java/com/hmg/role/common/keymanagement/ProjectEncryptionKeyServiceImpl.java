package com.hmg.role.common.keymanagement;

import com.hmg.role.admin.project.Project;
import com.hmg.role.common.cdc.uploader.interfaces.ObjectStorageService;
import com.hmg.role.common.keymanagement.cleanup.ExpiredIdentityCleanerService;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;
import com.hmg.role.util.AuthorRequestScope;
import jakarta.annotation.PreDestroy;
import java.time.ZonedDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnExpression("T(java.lang.Boolean).valueOf('${cdc.enabled:false}')")
public class ProjectEncryptionKeyServiceImpl implements ProjectEncryptionKeyService {

    private static final ProjectEncryptionKeyMapper MAPPER = ProjectEncryptionKeyMapper.INSTANCE;

    private final ObjectStorageService storageService;
    private final ExpiredIdentityCleanerService cleanerService;

    private final KeyManagerService keyManagerService;

    private final Base64.Decoder decoder;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope scope;

    @Override
    public ProjectEncryptionKeyDto getAccessKey() {
        Project project = scope.getProject();

        ProjectEncryptionKeyDto secret = keyManagerService.getKey(project);
        if (secret == null) {
            return create(project);
        } else {
            if (isExpiring(secret)) {
                secret = update(project, secret);
            }

            return secret;
        }
    }

    @Override
    public ProjectEncryptionKeyDto create(Project project) {
        ProjectEncryptionKeyDto secrets = keyManagerService.createNewKey(project);
        return secrets;
    }

    private ProjectEncryptionKeyDto update(Project project, ProjectEncryptionKeyDto secrets) {
        ProjectEncryptionKeyDto newSecrets = keyManagerService.updateKey(project.getKey(), secrets);
        byte[] oldKey = decoder.decode(secrets.getEncryptionKey());
        byte[] newKey = decoder.decode(newSecrets.getEncryptionKey());
        storageService.reencrypt(project.getKey(), oldKey, newKey);
        return newSecrets;
    }

    @Override
    public void delete(String projectKey) {
        storageService.deleteProjectDirectory(projectKey);
        keyManagerService.deleteKey(projectKey);
    }

    private static boolean isExpiring(ProjectEncryptionKeyDto secrets) {
        ZonedDateTime expiryLimit =
                ZonedDateTime.now().minus(KeyManagementUtils.CLIENT_IDENTITY_RENEWAL_GRACE_PERIOD);
        return secrets.expiredAt(expiryLimit);
    }

    @PreDestroy
    public void destroy() {
        cleanerService.shutdown();
    }
}
