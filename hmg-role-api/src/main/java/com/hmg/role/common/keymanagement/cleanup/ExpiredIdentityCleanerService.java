package com.hmg.role.common.keymanagement.cleanup;

import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.configuration.ProjectConfiguration;
import com.hmg.role.admin.project.configuration.ProjectConfigurationRepository;
import com.hmg.role.common.cdc.uploader.interfaces.ObjectStorageService;
import com.hmg.role.common.keymanagement.KeyManagementUtils;
import com.hmg.role.common.keymanagement.KeyManagerService;
import com.hmg.role.common.keymanagement.ProjectEncryptionKeyMapper;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnExpression("T(java.lang.Boolean).valueOf('${cdc.enabled:false}')")
public class ExpiredIdentityCleanerService {
    private static final long INITIAL_DELAY_NUMBER = 1; // delay period to allow boot up
    private static final long DELAY_NUMBER = 30;
    private static final TimeUnit DELAY_UNIT = TimeUnit.MINUTES;

    private final KeyManagerService keyManagerService;
    private final ObjectStorageService objectStorageService;

    private final ProjectConfigurationRepository configRepo;

    private final ScheduledFuture<?> jobHandle;
    private final ScheduledExecutorService scheduleExecutor;

    private final ProjectEncryptionKeyMapper mapper;
    private final Base64.Encoder encoder;
    private final Base64.Decoder decoder;

    @Autowired
    public ExpiredIdentityCleanerService(
            KeyManagerService keyManagerService,
            ObjectStorageService objectStorageService,
            ProjectConfigurationRepository configRepo,
            Base64.Encoder encoder,
            Base64.Decoder decoder) {
        this.keyManagerService = keyManagerService;
        this.objectStorageService = objectStorageService;
        this.configRepo = configRepo;

        this.scheduleExecutor = new ScheduledThreadPoolExecutor(1);
        this.jobHandle =
                this.scheduleExecutor.scheduleWithFixedDelay(
                        this::scheduledCleanup, INITIAL_DELAY_NUMBER, DELAY_NUMBER, DELAY_UNIT);
        this.mapper = ProjectEncryptionKeyMapper.INSTANCE;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public void scheduledCleanup() {
        log.info(
                "expired credential cleanup is running by schedule, time: {}",
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()));
        List<ProjectConfiguration> expireds =
                configRepo.getExpiredSecurityCredentials(
                        KeyManagementUtils.SECRET_PROPERTY_KEY_NAME,
                        ZonedDateTime.now()
                                .minus(KeyManagementUtils.CLIENT_IDENTITY_EXPIRY_GRACE_PERIOD)
                                .withZoneSameInstant(ZoneId.of("UTC"))
                                .toLocalDateTime());

        log.info("got: {} credentials expired", expireds.size());

        for (ProjectConfiguration expired : expireds) {
            log.info("regenerating project key: {}", expired.getProject().getKey());
            Project project = expired.getProject();
            String projectKey = project.getKey();
            ProjectEncryptionKeyDto secrets = mapper.fromMap(expired.getConfigurationValue());

            ProjectEncryptionKeyDto newSecrets = keyManagerService.updateKey(projectKey, secrets);
            byte[] oldKey = decoder.decode(secrets.getEncryptionKey());
            byte[] newKey = decoder.decode(newSecrets.getEncryptionKey());

            objectStorageService.reencrypt(projectKey, oldKey, newKey);

            log.info("expired credential for project: {} was updated by schedule", projectKey);
        }
    }

    public void shutdown() {
        //        jobHandle.cancel(false);
        scheduleExecutor.shutdown();
        configRepo.flush();
    }
}
