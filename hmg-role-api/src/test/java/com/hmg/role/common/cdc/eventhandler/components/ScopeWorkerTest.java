// src/test/java/com/hmg/role/cdc/eventhandler/components/ScopeWorkerTest.java
package com.hmg.role.common.cdc.eventhandler.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hmg.role.common.cdc.dto.CdcEventDto;
import com.hmg.role.common.cdc.enums.CdcEventType;
import com.hmg.role.common.cdc.io.FileManager;
import com.hmg.role.testcommon.MockPublisher;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.BeforeTestClass;

/**
 * Unit tests for ScopeWorker using: - JUnit Jupiter - Mockito (only for constructor parameters
 * except ApplicationEventPublisher) - Real internal virtual-thread executor and locks
 */
@ExtendWith(MockitoExtension.class)
class ScopeWorkerTest {

    // ---- constructor parameters (mocked) ----
    @Mock private FileManager fileManager;
    @Mock private ScheduledExecutorService debounceScheduler;
    @Mock private CsvHeaderExtractor headerExtractor;
    @Mock private CsvRowBuilder rowBuilder;
    @Mock private ProjectMetadataService metadataService;

    // ---- the test subject
    private ScopeWorker tested;

    // ---- backings ----
    private MockPublisher mockPublisher;

    private static final String project = "proj";
    private static final String scope = "scope";

    private static String csv() {
        return project + "/" + scope + ".csv";
    }

    private static String writing() {
        return project + "/" + scope + ".csv.writing";
    }

    private static String uploading() {
        return project + "/" + scope + ".csv.uploading";
    }

    private static String tmp() {
        return project + "/" + scope + ".csv.tmp";
    }

    private static String tempAlias() {
        return project + "/" + scope + ".csv.temp";
    }

    private AtomicReference<Runnable> scheduledFinalize;
    private ScheduledFuture<?> scheduledFutureMock;

    private AutoCloseable mocksHandle;

    @BeforeTestClass
    void setUp() {
        mocksHandle = MockitoAnnotations.openMocks(this);
    }

    @AfterTestClass
    void tearDown() throws Exception {
        mocksHandle.close();
    }

    @BeforeEach
    void beforeEach() {
        mockPublisher = new MockPublisher();
        scheduledFutureMock = mock(ScheduledFuture.class);

        when(fileManager.getBasePath()).thenReturn(Paths.get("/data", "server"));

        when(debounceScheduler.schedule(any(Callable.class), anyLong(), any(TimeUnit.class)))
                .thenAnswer(this::scheduleFutureTask);

        scheduledFinalize = new AtomicReference<>();

        tested =
                new ScopeWorker(
                        project,
                        scope,
                        mockPublisher,
                        fileManager,
                        debounceScheduler,
                        headerExtractor,
                        rowBuilder,
                        metadataService,
                        50L);
    }

    @AfterEach
    void afterEach() throws Exception {
        reset(fileManager, debounceScheduler, headerExtractor, rowBuilder, metadataService);
    }

    @Test
    void writeFlow_appendsHeaderAndRow_publishesBegin_andSchedulesFinalize() throws Exception {
        // Given
        when(fileManager.fileExists(csv())).thenReturn(false);
        when(fileManager.fileExists(writing())).thenReturn(false, true, true);
        when(fileManager.fileExists(uploading())).thenReturn(false);
        List<String> headers = List.of("a", "b");
        when(headerExtractor.extract(any(JsonNode.class))).thenReturn(headers);
        when(rowBuilder.buildFromPayload(any(JsonNode.class), eq(headers))).thenReturn("1,2");

        // When
        JsonNode key = keyWithPolicyItemId("pi-1");
        JsonNode value = valueWithOpAndFields("c", null);
        tested.submitRecord(key, value);

        // Then: begin event present in publisher (object event)
        // ScopeWorker sends CdcEventDto via publishEvent(Object)
        awaitEventsCount(1);
        assertThat(mockPublisher.getObjects())
                .anyMatch(
                        o ->
                                o instanceof CdcEventDto
                                        && ((CdcEventDto) o).getEventType()
                                                == CdcEventType.CDC_WRITING_BEGIN);

        // Header + row appended to WRITING
        ArgumentCaptor<String> appendContent = ArgumentCaptor.forClass(String.class);
        verify(fileManager, timeout(1000).atLeast(1))
                .append(eq(writing()), appendContent.capture());
        assertThat(appendContent.getAllValues()).contains("1,2"); // data row exists

        // Trigger finalize task synchronously
        Runnable debounceTask = scheduledFinalize.get();
        Assertions.assertNotNull(debounceTask, "Finalize task should have been scheduled");
        debounceTask.run();

        // Finalize rename & metadata update
        verify(fileManager, timeout(1000)).rename(eq(writing()), eq(csv()));
        verify(metadataService, timeout(1000)).updateProjectMetadata(project, scope);

        // End event present
        awaitEventsCount(2);
        assertThat(mockPublisher.getObjects())
                .anyMatch(
                        o ->
                                o instanceof CdcEventDto
                                        && ((CdcEventDto) o).getEventType()
                                                == CdcEventType.CDC_WRITING_END);
    }

    @Test
    void deleteFlow_opD_usesKeyToken_andDoesNotAppend() throws IOException {
        when(fileManager.fileExists(csv())).thenReturn(false);
        when(fileManager.fileExists(writing())).thenReturn(true);

        JsonNode key = keyWithPolicyItemId("pi-123");
        ObjectNode fields = JsonNodeFactory.instance.objectNode();
        JsonNode value = valueWithOpAndFields("d", fields);
        tested.submitRecord(key, value);

        // Begin event should be present
        awaitEventsCount(1);
        System.out.println("captured: " + mockPublisher.getAll().size() + " objects");
        assertThat(mockPublisher.getObjects())
                .anyMatch(
                        o ->
                                o instanceof CdcEventDto
                                        && ((CdcEventDto) o).getEventType()
                                                == CdcEventType.CDC_WRITING_BEGIN);

        // deleteLine invoked with token; no row append
        verify(fileManager, timeout(1000))
                .deleteLine(eq(writing()), aryEq(new String[] {"pi-123"}));
        verify(fileManager, after(300).never()).append(eq(writing()), anyString());
    }

    @Test
    void uploadingPath_writesViaTmp_merges_andCleansAliasAndTmp() throws IOException {
        when(fileManager.fileExists(csv())).thenReturn(false);
        when(fileManager.fileExists(eq(uploading()))).thenReturn(true);
        when(fileManager.fileExists(eq(tempAlias()))).thenReturn(true);
        when(fileManager.fileExists(eq(writing()))).thenReturn(false, true);

        List<String> headers = List.of("id", "name");
        when(headerExtractor.extract(any(JsonNode.class))).thenReturn(headers);
        when(rowBuilder.buildFromPayload(any(JsonNode.class), eq(headers))).thenReturn("10,Ada");

        ObjectNode fields = JsonNodeFactory.instance.objectNode();
        fields.put("is_deleted", 0);
        JsonNode key = keyWithPolicyItemId("pi-10");
        JsonNode value = valueWithOpAndFields("u", fields);
        tested.submitRecord(key, value);

        // Block until processRecord + rescheduleFinalize complete in the executor thread,
        // so all stubs are consumed before MockitoExtension's afterEach checks them.
        verify(debounceScheduler, timeout(1000))
                .schedule(any(Callable.class), anyLong(), any(TimeUnit.class));

        // Verify uploading path: row written to TMP, merged into WRITING, temporaries cleaned up
        verify(fileManager).append(eq(tmp()), eq("10,Ada"));
        verify(fileManager).appendAll(eq(tmp()), eq(tempAlias()));
        verify(fileManager).delete(eq(tempAlias()));
        verify(fileManager).appendAll(eq(writing()), eq(tmp()));
        verify(fileManager).delete(eq(tmp()));
    }

    // ---- helpers ------------------------------------------------------------

    // --- small helper to wait for events pumped by the virtual-thread executor ---
    private void awaitEventsCount(int expected) {
        long deadline = System.currentTimeMillis() + 1500;
        while (System.currentTimeMillis() < deadline) {
            if (mockPublisher.getInvocationCount() >= expected) return;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) { // NOSONAR: no need to propagate
                // God, Sonar,
                // this is a unit test,
                // why are you so naggy
                System.out.println("thread interrupted");
                e.printStackTrace(); // NOSONAR: no. This is a unit test; won't run in the app
            }
        }
        // give an informative assertion if it times out
        Assertions.assertEquals(
                expected,
                mockPublisher.getInvocationCount(),
                "Timed out waiting for " + expected + " published events");
    }

    private static JsonNode keyWithPolicyItemId(String id) {
        ObjectNode keyPayload = JsonNodeFactory.instance.objectNode();
        keyPayload.put("policy_item_id", id);
        ObjectNode key = JsonNodeFactory.instance.objectNode();
        key.set("payload", keyPayload);
        return key;
    }

    private static JsonNode valueWithOpAndFields(String op, ObjectNode fields) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("__op", op);
        if (fields != null) payload.setAll(fields);
        ObjectNode value = JsonNodeFactory.instance.objectNode();
        value.set("payload", payload);
        return value;
    }

    private ScheduledFuture<?> scheduleFutureTask(InvocationOnMock z) {
        Callable<?> callable = z.getArgument(0, Callable.class);
        scheduledFinalize.set(new FutureTask<>(callable));
        return scheduledFutureMock;
    }

    // ------------------------------------------------------------------------
}
