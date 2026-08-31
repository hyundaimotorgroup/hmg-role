package com.hmg.role.common.cdc.uploader;

import com.hmg.role.admin.project.ProjectRepository;
import com.hmg.role.common.cdc.dto.CdcEventDto;
import com.hmg.role.common.cdc.enums.CdcEventType;
import com.hmg.role.common.cdc.uploader.crypto.DataEncryptionService;
import com.hmg.role.common.cdc.uploader.interfaces.ObjectStorageService;
import com.hmg.role.common.keymanagement.KeyManagerService;
import com.hmg.role.sdk.common.util.Utils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnExpression("T(java.lang.Boolean).valueOf('${cdc.enabled:false}')")
public class ObjectStorageServiceImpl implements ObjectStorageService {
    private final DataEncryptionService encryptionService;

    private final S3Client s3;
    private final ApplicationEventPublisher publisher;

    // upload is event-triggered,
    // there's no way to get the encryption key other than from the table
    private final ProjectRepository projectRepository;
    private final KeyManagerService keyManagerService;

    private final Base64.Decoder decoder;

    @Value("${cdc.s3.bucket}")
    @Setter
    private String bucket;

    private final ExecutorService threadPool =
            new ScheduledThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    Thread.ofVirtual().name("upload-event-handler-", 0).factory());

    // prevent trampling between uploading and rotation
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public void pushChanges(CdcEventDto event) {
        Objects.requireNonNull(event, "CdcEventDto must not be null");

        final String project = event.getProject();
        final String scope = event.getScope();
        final Path basePath = event.getBasePath();
        final String file =
                Objects.requireNonNull(event.getFileName(), "fileName must not be null");

        // Resolve disk paths
        final Path source = basePath.resolve(file);
        final Path uploading = basePath.resolve(file + ".uploading");

        // S3 key is project/fileName
        String fileName = Path.of(file).getFileName().toString();
        String projectSha256 = getProjectSha256(project);
        final String s3Key = projectSha256 + "/" + fileName;

        try {
            // Validate file and s3 client
            validateFile(source);

            renameForLock(source, uploading);

            // Publish CDC_UPLOADING_BEGIN
            publisher.publishEvent(
                    CdcEventDto.builder()
                            .eventType(CdcEventType.CDC_UPLOADING_BEGIN)
                            .project(project)
                            .scope(scope)
                            .fileName(file)
                            .basePath(basePath)
                            .build());

            // Upload scope file: {project}/{scope}.csv
            putCsv(project, uploading, s3Key);

            // Publish CDC_UPLOADING_END
            publisher.publishEvent(
                    CdcEventDto.builder()
                            .eventType(CdcEventType.CDC_UPLOADING_END)
                            .project(project)
                            .scope(scope)
                            .fileName(file)
                            .basePath(basePath)
                            .build());

        } catch (Exception e) {
            log.error(
                    "Failed uploading scope={} to bucket={}, project={}: {}",
                    scope,
                    bucket,
                    project,
                    e.getMessage(),
                    e);
            // You may publish a failure event or rethrow as needed
            throw new RuntimeException("CDC upload failed, " + event, e);
        } finally {
            // Cleanup: rename back {scope}.csv.uploading -> {scope}.csv
            try {
                renameBack(uploading, source);
            } catch (IOException ioEx) {
                log.error("Cleanup rename failed for {}", uploading, ioEx);
            }
        }
    }

    @Override
    public void deleteProjectDirectory(String projectKey) {
        String projectKeyHash = getProjectSha256(projectKey);
        DeleteObjectRequest req =
                DeleteObjectRequest.builder().bucket(bucket).key(projectKeyHash).build();
        s3.deleteObject(req);
    }

    @Override
    public void reencrypt(String projectKey, byte[] oldKey, byte[] newKey) {
        String projectKeyHash = getProjectSha256(projectKey);
        log.info("reencrypting project data: {}, {}", projectKey, projectKeyHash);
        ListObjectsV2Request listParam =
                ListObjectsV2Request.builder()
                        .bucket(bucket)
                        .delimiter("/")
                        .prefix(projectKeyHash + "/")
                        .build();
        log.info("listing objects in {} bucket", listParam);
        ListObjectsV2Response resp = s3.listObjectsV2(listParam);

        for (var file : resp.contents()) {
            String fileName = file.key();
            reencryptFile(fileName, oldKey, newKey);
        }
        log.info("finished reencrypting project data: {}", projectKey);
    }

    public void reencryptFile(String fileName, byte[] oldKey, byte[] newKey) {
        log.info("reencrypting file: {}", fileName);
        var dlReq = GetObjectRequest.builder().bucket(bucket).key(fileName).build();
        var ulReq = PutObjectRequest.builder().bucket(bucket).key(fileName).build();
        var lock = locks.computeIfAbsent(fileName, k -> new ReentrantLock());
        lock.lock();
        try {
            // block uploads when reencrypting
            // and vice versa
            lock.lock();

            byte[] file = s3.getObject(dlReq).readAllBytes();
            byte[] reencryptedFile = encryptionService.reencrypt(file, oldKey, newKey);
            s3.putObject(ulReq, RequestBody.fromBytes(reencryptedFile));
        } catch (IOException e) {
            log.error("Get object failed for {}", fileName, e);
        } finally {
            lock.unlock();
            locks.remove(fileName);
            log.info("unlocked: {}", fileName);
        }
    }

    private void validateFile(Path file) {
        if (!Files.exists(file)) {
            throw new IllegalStateException("Missing file: " + file);
        }
        if (!Files.isReadable(file)) {
            throw new IllegalStateException("Unreadable file: " + file);
        }
    }

    private void renameForLock(Path from, Path to) throws IOException {
        if (!Files.exists(from)) {
            throw new IOException("Source CSV not found: " + from);
        }
        // Ensure previous uploading artifact is removed or handled
        if (Files.exists(to)) {
            Files.delete(to);
        }
        Files.move(from, to);
        log.debug("Locked file: {} -> {}", from, to);
    }

    private void putCsv(String project, Path path, String objectKey) throws Exception {
        RequestBody b;
        if (Path.of(objectKey).getFileName().toString().equalsIgnoreCase("metadata.csv")) {
            // don't encrypt metadata file
            b = RequestBody.fromFile(path);
        } else {
            String encryptionKeyB64 = getProjectEncryptionKey(project);
            if (encryptionKeyB64 == null) {
                log.warn("No encryption key found for {}", project);
                return; // TODO handle when project have no encryption key
            }
            byte[] encryptionKey = decoder.decode(encryptionKeyB64);
            byte[] plaintext = Files.readAllBytes(path);
            byte[] ciphertext = encryptionService.encrypt(plaintext, encryptionKey);
            b = RequestBody.fromBytes(ciphertext);
        }

        var lock = locks.computeIfAbsent(objectKey, k -> new ReentrantLock());
        lock.lock();
        try {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType("text/csv")
                            .build(),
                    b);
            log.info("Uploaded {} to s3://{}/{}", path.getFileName(), bucket, objectKey);
        } catch (S3Exception me) {
            log.error("S3 error uploading {} to key {}: {}", path, objectKey, me.getMessage(), me);
            throw me;
        } finally {
            lock.unlock();
            locks.remove(objectKey);
            log.info("unlocked: {}", objectKey);
        }
    }

    private void renameBack(Path from, Path to) throws IOException {
        if (Files.exists(from)) {
            // Replace original if still exist
            if (Files.exists(to)) {
                Files.delete(to);
            }
            Files.move(from, to);
            log.debug("Unlocked file: {} -> {}", from, to);
        }
    }

    private String getProjectEncryptionKey(String projectKey) {
        var project = projectRepository.getByKey(projectKey);
        var projectSecurityInfo = keyManagerService.getKey(project);

        if (projectSecurityInfo == null) {
            log.warn("Project key: {} have no encryption key yet", projectKey);
            var secInfo = keyManagerService.createNewKey(project);
            return secInfo.getEncryptionKey();
        } else {
            return projectSecurityInfo.getEncryptionKey();
        }
    }

    private static String getProjectSha256(String project) {
        return Utils.sha256(project);
    }
}
