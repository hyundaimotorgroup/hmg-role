package com.hmg.role.common.keymanagement;

import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.configuration.ProjectConfiguration;
import com.hmg.role.admin.project.configuration.ProjectConfigurationRepository;
import com.hmg.role.common.cdc.uploader.crypto.AesKeyGenerator;
import com.hmg.role.sdk.common.util.Utils;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;
import com.hmg.role.util.container.Pair;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class KeyManagerService {
    private static final ProjectEncryptionKeyMapper MAPPER = ProjectEncryptionKeyMapper.INSTANCE;

    private final ProjectConfigurationRepository confRepo;

    private final AesKeyGenerator keyGenerator;

    private final Pair<Period, Duration> secretExpiryDuration;
    private final Base64.Encoder encoder;
    private final Base64.Decoder decoder;

    public ProjectEncryptionKeyDto getKey(Project project) {
        var securityProp =
                confRepo.findByProjectAndConfigurationKey(
                        project, KeyManagementUtils.SECRET_PROPERTY_KEY_NAME);
        if (securityProp == null) {
            // the project doesn't have security config yet
            return null;
        }
        var secret = MAPPER.fromMap(securityProp.getConfigurationValue());
        return secret;
    }

    public synchronized ProjectEncryptionKeyDto createNewKey(Project project) {
        log.warn("Creating key for project: {}", project.getKey());
        byte[] encryptionKey = keyGenerator.generateRawAes256Key();
        String encryptionKeyB64 = encoder.encodeToString(encryptionKey);

        ProjectEncryptionKeyDto secrets =
                new ProjectEncryptionKeyDto(
                        encryptionKeyB64, Utils.formatToIso8601String(getNewExpiry()));

        ProjectConfiguration securityProp = new ProjectConfiguration();
        securityProp.setProject(project);
        securityProp.setConfigurationKey(KeyManagementUtils.SECRET_PROPERTY_KEY_NAME);
        securityProp.setConfigurationValue(MAPPER.fromDto(secrets));
        confRepo.save(securityProp);
        return secrets;
    }

    public ProjectEncryptionKeyDto updateKey(String projectKey, ProjectEncryptionKeyDto secrets) {
        byte[] newKey = keyGenerator.generateRawAes256Key();

        String newKeyB64 = encoder.encodeToString(newKey);
        ProjectEncryptionKeyDto newSecrets =
                new ProjectEncryptionKeyDto(newKeyB64, Utils.formatToIso8601String(getNewExpiry()));

        var config =
                confRepo.findByProjectKeyAndConfigurationKey(
                        projectKey, KeyManagementUtils.SECRET_PROPERTY_KEY_NAME);
        config.setConfigurationValue(MAPPER.fromDto(newSecrets));
        confRepo.save(config);

        return newSecrets;
    }

    public void deleteKey(String projectKey) {
        ProjectConfiguration toBeDeleted =
                confRepo.findByProjectKeyAndConfigurationKey(
                        projectKey, KeyManagementUtils.SECRET_PROPERTY_KEY_NAME);
        confRepo.delete(toBeDeleted);
    }

    private ZonedDateTime getNewExpiry() {
        return ZonedDateTime.now()
                .plus(secretExpiryDuration.first())
                .plus(secretExpiryDuration.second())
                .truncatedTo(ChronoUnit.SECONDS)
                .withZoneSameInstant(ZoneId.of("UTC"));
    }
}
