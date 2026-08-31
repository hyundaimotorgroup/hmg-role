package com.hmg.role.common.cdc.eventhandler.components;

import com.hmg.role.common.cdc.io.FileManager;
import com.hmg.role.common.cdc.utils.CdcUtils;
import com.hmg.role.sdk.common.util.Utils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Maintains project/metadata.csv (file_name,last_updated_at,sha256_checksum). */
@Slf4j
@RequiredArgsConstructor
public class ProjectMetadataService {

    private final FileManager fileManager;

    // Serialize metadata.csv updates per project
    private final ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public void updateProjectMetadata(String project, String scope) {
        var lock = locks.computeIfAbsent(project, p -> new ReentrantLock());
        lock.lock();
        try {
            final String metadataFile = project + "/metadata.csv";
            final String fileNameOnly = scope + ".csv";

            ensureMetadataHeader(project);

            Path csvPath =
                    fileManager.getBasePath().resolve(project).resolve(fileNameOnly).normalize();
            if (!Files.exists(csvPath)) {
                log.warn("[{}-{}] CSV not found for metadata update: {}", project, scope, csvPath);
                return;
            }

            String lastUpdated = safeFileLastModifiedIso(csvPath);
            String sha256 = computeSha256Hex(csvPath);

            // Delete old line (prefix match) and append fresh line
            String linePrefix = fileNameOnly + ",";
            try {
                fileManager.deleteLine(metadataFile, linePrefix);
            } catch (IOException e) {
                log.debug("[{}] metadata deleteLine warn: {}", metadataFile, e.getMessage());
            }

            String metaLine =
                    String.join(
                            ",",
                            List.of(
                                    CdcUtils.csvEscape(fileNameOnly),
                                    CdcUtils.csvEscape(lastUpdated),
                                    CdcUtils.csvEscape(sha256)));
            fileManager.append(metadataFile, metaLine);

        } catch (Exception e) {
            log.error(
                    "[{}-{}] Failed to update metadata.csv: {}", project, scope, e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    private void ensureMetadataHeader(String project) throws IOException {
        Path metadataPath =
                fileManager.getBasePath().resolve(project).resolve("metadata.csv").normalize();
        if (!Files.exists(metadataPath)) {
            Files.createDirectories(metadataPath.getParent());
            String header = "file_name,last_updated_at,sha256_checksum";
            fileManager.append(project + "/metadata.csv", header);
        }
    }

    private String safeFileLastModifiedIso(Path path) {
        try {
            FileTime ft = Files.getLastModifiedTime(path);
            return ft.toInstant().toString();
        } catch (IOException e) {
            return Instant.now().toString();
        }
    }

    private String computeSha256Hex(Path path) throws IOException {
        byte[] file = fileManager.readFile(path.toString());
        return Utils.sha256(file);
    }
}
