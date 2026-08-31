package com.hmg.role.common.cdc.eventhandler.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.hmg.role.common.cdc.dto.CdcEventDto;
import com.hmg.role.common.cdc.enums.CdcEventType;
import com.hmg.role.common.cdc.io.FileManager;
import com.hmg.role.common.cdc.utils.CdcUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Per-scope single-thread worker (virtual thread) with per-scope debounce finalize. Implements __op
 * routing (c/r/u/d) for unwrapped Debezium payload and ensures that ALL ops go through the same
 * file naming & guarding procedures (lock, begin, finalize).
 */
@Slf4j
@EqualsAndHashCode
public class ScopeWorker {

    private final String project;
    private final String scope;
    private final ApplicationEventPublisher eventPublisher;
    private final FileManager fileManager;
    private final ScheduledExecutorService debounceScheduler;
    private final CsvHeaderExtractor headerExtractor;
    private final CsvRowBuilder rowBuilder;
    private final ProjectMetadataService metadataService;
    private final long debounceMillis;

    // Single-thread executor per (project,scope) to preserve file operation ordering (virtual
    // thread)
    private final ExecutorService executor;

    private final ReentrantLock stateLock;
    private ScheduledFuture<?> finalizeFuture;

    // Write-cycle state
    private final AtomicBoolean inCycle;

    private List<String> headers;
    private boolean headerWrittenToWriting = false;

    public ScopeWorker(
            String project,
            String scope,
            ApplicationEventPublisher eventPublisher,
            FileManager fileManager,
            ScheduledExecutorService debounceScheduler,
            CsvHeaderExtractor headerExtractor,
            CsvRowBuilder rowBuilder,
            ProjectMetadataService metadataService,
            long debounceMillis) {
        this.project = project;
        this.scope = scope;
        this.eventPublisher = eventPublisher;
        this.fileManager = fileManager;
        this.debounceScheduler = debounceScheduler;
        this.headerExtractor = headerExtractor;
        this.rowBuilder = rowBuilder;
        this.metadataService = metadataService;
        this.debounceMillis = debounceMillis;
        this.stateLock = new ReentrantLock();
        this.headers = Collections.synchronizedList(new LinkedList<>());
        this.inCycle = new AtomicBoolean(false);
        this.executor =
                Executors.newSingleThreadExecutor(
                        Thread.ofVirtual().name("cdc-scope-" + hashCode(), 0).factory());
    }

    public void shutdown() {
        stateLock.lock();
        try {
            if (finalizeFuture != null) finalizeFuture.cancel(false);
        } finally {
            stateLock.unlock();
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) { // NOSONAR: no need to propagate
            log.warn("Interrupted", e);
        }
    }

    public void submitRecord(JsonNode key, JsonNode value) {
        executor.submit(
                () -> {
                    try {
                        processRecord(key, value);
                        // Debounce finalize for ALL ops (c/r/u/d)
                        rescheduleFinalize();
                    } catch (Exception e) {
                        log.error(
                                "[{}-{}], error processing record. {}",
                                project,
                                scope,
                                e.getMessage(),
                                e);
                    }
                });
    }

    /** Core logic: lock, BEGIN (for all ops), route by __op (c/r/u/d), write/merge, finalize. */
    private void processRecord(JsonNode key, JsonNode value) throws IOException {
        final String CSV = project + "/" + scope + ".csv";
        final String WRITING = project + "/" + scope + ".csv.writing";
        final String UPLOADING = project + "/" + scope + ".csv.uploading";
        final String TMP = project + "/" + scope + ".csv.tmp";
        final String TEMP_ALIAS = project + "/" + scope + ".csv.temp";

        JsonNode payload = value.get("payload");
        if (payload == null || payload.isNull()) return;

        // 1) Lock: csv -> writing (always for all ops)
        if (fileManager.fileExists(CSV) && !fileManager.fileExists(WRITING)) {
            try {
                fileManager.rename(CSV, WRITING);
                log.debug("[{}] Locked {} -> {}", scope, CSV, WRITING);
            } catch (IOException e) {
                log.warn("[{}] Lock rename failed (proceeding): {}", scope, e.getMessage());
            }
        }

        // 2) Begin cycle (for ALL ops, including DELETE)
        if (inCycle.compareAndSet(false, true)) {
            publishBegin();
            // If WRITING already exists, we assume header has been written earlier
            headerWrittenToWriting = fileManager.fileExists(WRITING);
        }

        String op = payload.path("__op").asText("").toLowerCase();
        boolean softDelete = "u".equals(op) && payload.path("is_deleted").asInt(0) == 1;
        String policyItemIdFromKey = key.path("payload").path("policy_item_id").asText();
        boolean hardDeleteFlattened =
                policyItemIdFromKey != null && !policyItemIdFromKey.isBlank() && payload.isNull();

        // ---------- DELETE path ----------
        if ("d".equals(op) || softDelete || hardDeleteFlattened) {
            List<String> tokens;
            if (policyItemIdFromKey != null && !policyItemIdFromKey.isBlank()) {
                tokens = List.of(policyItemIdFromKey);
            } else {
                tokens = CdcUtils.buildCdcDeleteToken(payload);
            }

            if (tokens.isEmpty()) {
                log.warn(
                        "[{}-{}] DELETE without usable tokens; skipping. payload={}",
                        project,
                        scope,
                        payload);
                return;
            }
            if (!fileManager.fileExists(WRITING)) {
                // If WRITING doesn't exist now, there's nothing to remove safely (keep contract)
                log.warn(
                        "[{}-{}] WRITING not found during DELETE; skipping tokens={}",
                        project,
                        scope,
                        tokens);
                return;
            }
            try {
                fileManager.deleteLine(
                        WRITING, tokens.toArray(new String[0])); // multi-contain delete
                log.debug(
                        "[{}-{}] Deleted rows containing tokens {} from {}",
                        project,
                        scope,
                        tokens,
                        WRITING);
            } catch (IOException e) {
                log.error(
                        "[{}-{}] Failed to delete tokens {}: {}",
                        project,
                        scope,
                        tokens,
                        e.getMessage());
            }
            return; // For DELETE ops, no header/row appends
        }

        // ---------- WRITE path: c, r, u ----------
        // 3) Header discovery (before first data row)
        if (headers.isEmpty()) {
            headers = headerExtractor.extract(value);
            if (headers.isEmpty()) {
                log.warn("[{}] No headers discovered; skipping row to avoid empty lines.", scope);
                return;
            }
        }

        // 4) Ensure header exists at top of WRITING before any data rows in this cycle.
        if (!headerWrittenToWriting) {
            if (!fileManager.fileExists(WRITING)) {
                appendNonBlank(WRITING, CdcUtils.toCsvLine(headers));
                headerWrittenToWriting = true;
            } else {
                headerWrittenToWriting = true; // assume header already present
            }
        }

        // 5) Build new row
        String row = rowBuilder.buildFromPayload(payload, headers);
        if (row == null || row.isBlank()) {
            log.debug("[{}] Skipping blank row.", scope);
            return;
        }

        // 6) For UPDATE: remove old row(s) then append the new row
        if ("u".equals(op)) {
            List<String> tokens = CdcUtils.buildCdcDeleteToken(payload);
            if (!tokens.isEmpty() && fileManager.fileExists(WRITING)) {
                try {
                    fileManager.deleteLine(WRITING, tokens.toArray(new String[0]));
                    log.debug(
                            "[{}-{}] UPDATE: removed old row(s) containing tokens {}",
                            project,
                            scope,
                            tokens);
                } catch (IOException e) {
                    log.warn(
                            "[{}-{}] UPDATE delete failed for tokens {}: {}",
                            project,
                            scope,
                            tokens,
                            e.getMessage());
                }
            }
        }

        // 7) Conditional write: uploading vs direct
        boolean uploading = fileManager.fileExists(UPLOADING);
        if (uploading) {
            appendNonBlank(TMP, row);

            if (fileManager.fileExists(TEMP_ALIAS)) {
                fileManager.appendAll(TMP, TEMP_ALIAS);
                try {
                    fileManager.delete(TEMP_ALIAS);
                } catch (Exception e) {
                    log.error(
                            "[{}-{}] Failed to delete temp alias: {}",
                            project,
                            scope,
                            TEMP_ALIAS,
                            e);
                }
            }

            fileManager.appendAll(WRITING, TMP);
            try {
                fileManager.delete(TMP);
            } catch (Exception e) {
                log.error("[{}-{}] Failed to delete temp: {}", project, scope, TMP, e);
            }

        } else {
            appendNonBlank(WRITING, row);
        }
    }

    private void rescheduleFinalize() {
        if (debounceMillis <= 0) return;
        stateLock.lock();
        try {
            if (finalizeFuture != null) finalizeFuture.cancel(false);
            finalizeFuture =
                    debounceScheduler.schedule(
                            () -> executor.submit(this::finalizeCycleSafe),
                            debounceMillis,
                            TimeUnit.MILLISECONDS);
        } finally {
            stateLock.unlock();
        }
    }

    private void finalizeCycleSafe() {
        try {
            finalizeCycle();
        } catch (Exception e) {
            log.error("[{}-{}], error during finalize", project, scope, e);
        }
    }

    private void finalizeCycle() {
        final String CSV = project + "/" + scope + ".csv";
        final String WRITING = project + "/" + scope + ".csv.writing";

        if (fileManager.fileExists(WRITING)) {
            try {
                fileManager.rename(WRITING, CSV);
                log.debug("[{}] Finalized: {} -> {}", scope, WRITING, CSV);
                // Update per-project metadata on every finalize
                publishBeginMetadata();
                metadataService.updateProjectMetadata(project, scope);
                publishEndMetadata();
            } catch (IOException e) {
                log.warn(
                        "[{}] Could not finalize rename {} -> {}: {}",
                        scope,
                        WRITING,
                        CSV,
                        e.getMessage(),
                        e);
            }
        }

        if (inCycle.compareAndSet(true, false)) {
            publishEnd();
        }

        stateLock.lock();
        try {
            finalizeFuture = null;
        } finally {
            stateLock.unlock();
        }
    }

    // ---- helpers ----

    private void appendNonBlank(String fileName, String content) throws IOException {
        if (content == null || content.isBlank()) {
            log.debug("[{}] Prevented blank append to {}.", scope, fileName);
            return;
        }
        fileManager.append(fileName, content);
    }

    private void publishBegin() {
        CdcEventDto event =
                CdcEventDto.builder()
                        .eventType(CdcEventType.CDC_WRITING_BEGIN)
                        .project(project)
                        .scope(scope)
                        .basePath(fileManager.getBasePath())
                        .fileName(Path.of(project, "%s.csv".formatted(scope)).toString())
                        .build();
        eventPublisher.publishEvent(event);
    }

    private void publishBeginMetadata() {
        CdcEventDto event =
                CdcEventDto.builder()
                        .eventType(CdcEventType.CDC_WRITING_BEGIN)
                        .project(project)
                        .basePath(fileManager.getBasePath())
                        .fileName(Path.of(project, "metadata.csv").toString())
                        .build();
        eventPublisher.publishEvent(event);
    }

    private void publishEnd() {
        CdcEventDto event =
                CdcEventDto.builder()
                        .eventType(CdcEventType.CDC_WRITING_END)
                        .project(project)
                        .scope(scope)
                        .basePath(fileManager.getBasePath())
                        .fileName(Path.of(project, "%s.csv".formatted(scope)).toString())
                        .build();
        eventPublisher.publishEvent(event);
    }

    private void publishEndMetadata() {
        CdcEventDto event =
                CdcEventDto.builder()
                        .eventType(CdcEventType.CDC_WRITING_END)
                        .project(project)
                        .basePath(fileManager.getBasePath())
                        .fileName(Path.of(project, "metadata.csv").toString())
                        .build();
        eventPublisher.publishEvent(event);
    }
}
