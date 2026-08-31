package com.hmg.role.common.cdc.eventhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmg.role.common.cdc.eventhandler.components.CsvHeaderExtractor;
import com.hmg.role.common.cdc.eventhandler.components.CsvRowBuilder;
import com.hmg.role.common.cdc.eventhandler.components.ProjectMetadataService;
import com.hmg.role.common.cdc.eventhandler.components.ScopeWorker;
import com.hmg.role.common.cdc.io.FileManager;
import com.hmg.role.util.container.Pair;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Thin orchestrator: groups records by (project_key, scope_key) and delegates to ScopeWorker. */
@Slf4j
@Component
public class CdcEventHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper mapper;
    private final FileManager fileManager;
    private final ScheduledExecutorService debounceScheduler;
    private final long debounceMillis;

    private final CsvHeaderExtractor headerExtractor;
    private final CsvRowBuilder rowBuilder;
    private final ProjectMetadataService metadataService;

    // Per-(project, scope) workers
    private final ConcurrentHashMap<Pair<String, String>, ScopeWorker> workers;

    public CdcEventHandler(
            ApplicationEventPublisher eventPublisher,
            ObjectMapper mapper,
            FileManager fileManager,
            ScheduledExecutorService debounceScheduler,
            CsvHeaderExtractor headerExtractor,
            CsvRowBuilder rowBuilder,
            ProjectMetadataService metadataService,
            @Value("${cdc.timer-grace-millis:100}") long debounceMillis) {
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
        this.fileManager = fileManager;
        this.debounceScheduler = debounceScheduler;
        this.headerExtractor = headerExtractor;
        this.rowBuilder = rowBuilder;
        this.metadataService = metadataService;
        this.debounceMillis = debounceMillis;

        workers = new ConcurrentHashMap<>();
    }

    /** Debezium callback */
    public void handleEvent(
            List<ChangeEvent<String, String>> records,
            DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer) {
        if (records == null || records.isEmpty()) {
            try {
                committer.markBatchFinished();
            } catch (Exception ignore) {
            }
            return;
        }

        for (ChangeEvent<String, String> record : records) {
            try {
                JsonNode key = mapper.readTree(record.key());
                JsonNode value = mapper.readTree(record.value());
                JsonNode payload = value.get("payload");
                String project = (payload == null) ? "" : payload.path("project_key").asText();
                String scope = (payload == null) ? "" : payload.path("scope_key").asText();

                Pair<String, String> changeEntryKey = new Pair<>(project, scope);
                ScopeWorker worker =
                        workers.computeIfAbsent(
                                changeEntryKey,
                                k ->
                                        new ScopeWorker(
                                                k.first(),
                                                k.second(),
                                                eventPublisher,
                                                fileManager,
                                                debounceScheduler,
                                                headerExtractor,
                                                rowBuilder,
                                                metadataService,
                                                debounceMillis));

                worker.submitRecord(key, value);
                committer.markProcessed(record);

            } catch (Exception e) {
                log.error(
                        "Failed to parse or enqueue record; marking processed. value={}",
                        record.value(),
                        e);
                try {
                    committer.markProcessed(record);
                } catch (Exception ignore) {
                }
            }
        }

        try {
            committer.markBatchFinished();
        } catch (Exception e) {
            log.error("Error during markBatchFinished()", e);
        }
    }

    /** Optional: stop workers & release scheduler if you own it */
    @PreDestroy
    public void shutdown() {
        workers.values().forEach(ScopeWorker::shutdown);
        workers.clear();
        try {
            debounceScheduler.awaitTermination(5, TimeUnit.SECONDS); // NOSONAR
        } catch (InterruptedException e) { // NOSONAR: no need to propagate
            log.error("Interrupted while waiting for debounce", e);
        }
    }
}
