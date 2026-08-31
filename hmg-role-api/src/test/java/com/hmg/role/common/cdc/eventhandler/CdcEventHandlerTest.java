package com.hmg.role.common.cdc.eventhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Unit tests for CdcEventHandler using JUnit Jupiter and Mockito. Only constructor parameters are
 * mocked; ChangeEvent and RecordCommitter are fakes.
 */
@ExtendWith(MockitoExtension.class)
class CdcEventHandlerTest {

    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private FileManager fileManager;
    @Mock private ScheduledExecutorService debounceScheduler;
    @Mock private CsvHeaderExtractor headerExtractor;
    @Mock private CsvRowBuilder rowBuilder;
    @Mock private ProjectMetadataService metadataService;

    // ---- the test subject
    private CdcEventHandler tested;

    // ---- backers
    private Map<Pair<String, String>, ScopeWorker> scopeWorkers;

    @BeforeEach
    void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        ObjectMapper mapper = new ObjectMapper();
        when(headerExtractor.extract(any(JsonNode.class)))
                .thenReturn(List.of("header1", "header2", "header3"));

        scopeWorkers = new ConcurrentHashMap<>();

        tested =
                new CdcEventHandler(
                        eventPublisher,
                        mapper,
                        fileManager,
                        debounceScheduler,
                        headerExtractor,
                        rowBuilder,
                        metadataService,
                        /* debounceMillis */ 100L);

        Field workerField = CdcEventHandler.class.getDeclaredField("workers");
        workerField.setAccessible(true);
        workerField.set(tested, scopeWorkers);
    }

    @AfterEach
    public void afterEach() {
        System.out.println("total worker count: " + scopeWorkers.size());
        reset(
                eventPublisher,
                fileManager,
                debounceScheduler,
                headerExtractor,
                rowBuilder,
                metadataService);
    }

    @Nested
    @DisplayName("handleEvent")
    class HandleEventTests {

        @Test
        @DisplayName("Null or empty records → markBatchFinished only")
        void handleEvent_nullOrEmpty_records_marksBatchFinished() throws Exception {
            RecordingCommitter committer = new RecordingCommitter();

            // null list
            tested.handleEvent(null, committer);
            assertTrue(
                    committer.batchFinished, "markBatchFinished should be called for null records");
            assertEquals(0, committer.processed.size(), "No records should be processed");

            // empty list
            RecordingCommitter committer2 = new RecordingCommitter();
            tested.handleEvent(List.of(), committer2);
            assertTrue(
                    committer2.batchFinished,
                    "markBatchFinished should be called for empty records");
            assertEquals(0, committer2.processed.size(), "No records should be processed");
        }

        @Test
        @DisplayName("Valid JSON → worker created, record processed, batch finished")
        void handleEvent_happyPath_singleRecord() throws Exception {
            RecordingCommitter committer = new RecordingCommitter();

            String keyJson = "{\"id\":\"1\"}";
            String valueJson = "{\"payload\":{\"project_key\":\"PRJ\",\"scope_key\":\"SCP\"}}";
            ChangeEvent<String, String> ev = new SimpleChangeEvent(keyJson, valueJson);

            tested.handleEvent(List.of(ev), committer);

            // committer side-effects
            assertEquals(
                    1, committer.processed.size(), "Exactly one record should be marked processed");
            assertTrue(committer.batchFinished, "Batch should be marked finished");

            // internal map assertions via reflection
            Map<Pair<String, String>, ScopeWorker> workers = getWorkers(tested);
            assertEquals(1, workers.size(), "One worker should be created");
            assertTrue(
                    workers.containsKey(new Pair<>("PRJ", "SCP")),
                    "Worker key should be (PRJ, SCP)");
        }

        @Test
        @DisplayName("Two records with same (project,scope) reuse the same worker")
        void handleEvent_reuseWorker_forSameKey() throws Exception {
            RecordingCommitter committer = new RecordingCommitter();

            ChangeEvent<String, String> ev1 =
                    new SimpleChangeEvent(
                            "{\"id\":\"1\"}",
                            "{\"payload\":{\"project_key\":\"PRJ\",\"scope_key\":\"SCP\"}}");
            ChangeEvent<String, String> ev2 =
                    new SimpleChangeEvent(
                            "{\"id\":\"2\"}",
                            "{\"payload\":{\"project_key\":\"PRJ\",\"scope_key\":\"SCP\"}}");

            // First batch: create worker
            tested.handleEvent(List.of(ev1), committer);
            Map<Pair<String, String>, ScopeWorker> workers1 = getWorkers(tested);
            ScopeWorker w1 = workers1.get(new Pair<>("PRJ", "SCP"));
            assertNotNull(w1, "Worker must exist after first event");

            // Second batch: same key → should reuse the same instance
            RecordingCommitter committer2 = new RecordingCommitter();
            tested.handleEvent(List.of(ev2), committer2);
            Map<Pair<String, String>, ScopeWorker> workers2 = getWorkers(tested);
            ScopeWorker w2 = workers2.get(new Pair<>("PRJ", "SCP"));

            assertSame(w1, w2, "The same ScopeWorker instance should be reused for identical key");
            assertEquals(
                    2,
                    committer.totalProcessedCount() + committer2.totalProcessedCount(),
                    "Two records should be processed across batches");
            assertTrue(
                    committer.batchFinished && committer2.batchFinished,
                    "Both batches should be finished");
        }

        @Test
        @DisplayName("Invalid JSON → markProcessed still called; no workers created")
        void handleEvent_invalidJson_marksProcessed_noWorkers() throws Exception {
            RecordingCommitter committer = new RecordingCommitter();

            // invalid value JSON to trigger ObjectMapper parsing error
            ChangeEvent<String, String> bad = new SimpleChangeEvent("{\"id\":\"x\"}", "{not json}");

            tested.handleEvent(List.of(bad), committer);

            assertEquals(
                    1,
                    committer.processed.size(),
                    "Record should still be marked processed on error");
            assertTrue(committer.batchFinished, "Batch should be marked finished even on error");

            Map<Pair<String, String>, ScopeWorker> workers = getWorkers(tested);
            assertEquals(0, workers.size(), "No workers should be created on parse error");
        }
    }

    @Test
    @DisplayName("shutdown clears workers and awaits scheduler termination")
    void shutdown_clearsWorkers_andAwaitsScheduler() throws Exception {
        // Prepare one worker by sending a valid event
        RecordingCommitter committer = new RecordingCommitter();
        ChangeEvent<String, String> ev =
                new SimpleChangeEvent(
                        "{\"id\":\"1\"}",
                        "{\"payload\":{\"project_key\":\"PRJ\",\"scope_key\":\"SCP\"}}");
        tested.handleEvent(List.of(ev), committer);

        // Sanity: a worker exists
        Map<Pair<String, String>, ScopeWorker> workersBefore = getWorkers(tested);
        assertFalse(workersBefore.isEmpty(), "Workers map should not be empty before shutdown");

        // Stub awaitTermination to return immediately
        when(debounceScheduler.awaitTermination(anyLong(), any(TimeUnit.class))).thenReturn(true);

        // Act
        tested.shutdown();

        // Assert: workers cleared and scheduler awaited
        Map<Pair<String, String>, ScopeWorker> workersAfter = getWorkers(tested);
        assertTrue(workersAfter.isEmpty(), "Workers map should be cleared after shutdown");
        verify(debounceScheduler, times(1)).awaitTermination(eq(5L), eq(TimeUnit.SECONDS));
    }

    // ---------- helpers & fakes ----------

    private Map<Pair<String, String>, ScopeWorker> getWorkers(CdcEventHandler h) {
        return scopeWorkers;
    }

    @Getter
    private static final class SimpleChangeEvent implements ChangeEvent<String, String> {
        private final String key;
        private final String value;

        SimpleChangeEvent(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public String destination() {
            return "";
        }

        @Override
        public Integer partition() {
            return 0;
        }
    }

    /** Minimal fake RecordCommitter that records calls (no Mockito). */
    private static final class RecordingCommitter
            implements DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> {
        final List<ChangeEvent<String, String>> processed = new LinkedList<>();
        boolean batchFinished = false;

        @Override
        public void markProcessed(ChangeEvent<String, String> record) {
            processed.add(record);
        }

        @Override
        public void markBatchFinished() {
            batchFinished = true;
        }

        @Override
        public void markProcessed(
                ChangeEvent<String, String> record, DebeziumEngine.Offsets sourceOffsets)
                throws InterruptedException {}

        @Override
        public DebeziumEngine.Offsets buildOffsets() {
            return null;
        }

        int totalProcessedCount() {
            return processed.size();
        }
    }
}
