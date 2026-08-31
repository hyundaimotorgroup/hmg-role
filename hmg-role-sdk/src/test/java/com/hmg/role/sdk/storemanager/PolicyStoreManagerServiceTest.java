package com.hmg.role.sdk.storemanager;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hmg.role.sdk.db.MapDbStore;
import com.hmg.role.sdk.fetcher.S3FetcherService;
import com.hmg.role.sdk.fetcher.dto.S3FetchResponseDto;
import com.hmg.role.sdk.fetcher.security.DecryptorService;
import com.hmg.role.sdk.reader.SdkCsvFileReader;
import com.hmg.role.sdk.reader.model.MetadatumModel;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@Tags({@Tag("sdk"), @Tag("unitTest"), @Tag("smallTest")})
@ExtendWith(MockitoExtension.class)
public class PolicyStoreManagerServiceTest {
    private AutoCloseable mockCloseable;

    @Mock private S3FetcherService sdkCsvFetcher;
    @Mock private SdkCsvFileReader fileReader;
    @Mock private MapDbStore store;
    @Mock private DecryptorService decryptorService;

    private static final String DUMB_PROJECT_KEY = "projectKey";

    public PolicyStoreManagerServiceTest() {}

    @BeforeEach
    public void setUpEach() {
        System.out.println("[setUp] Creating service with mocked dependencies...");
        mockCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDownEach() throws Exception {
        System.out.println("tearDownEach");
        Mockito.reset(sdkCsvFetcher, fileReader, store, decryptorService);
        mockCloseable.close();
    }

    @Test
    public void doUpdate_noUpdateNeeded_metadataNotNewer_doesNothing() throws Exception {
        System.out.println("[Test] doUpdate(): metadata not newer → no update should occur");

        // The service initializes lastMetadataUpdateTime to Instant.EPOCH (UTC).
        // Return the same (not newer) time so doMetadataCheck() returns false.
        OffsetDateTime notNewer = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
        when(sdkCsvFetcher.getLastUpdatedUtc(anyString())).thenReturn(notNewer);

        when(decryptorService.decrypt(any(byte[].class)))
                .thenReturn("scope,updatedAt\n".getBytes(StandardCharsets.UTF_8));

        PolicyStoreManagerService tested =
                PolicyStoreManagerService.builder()
                        .sdkCsvFetcher(sdkCsvFetcher)
                        .fileReader(fileReader)
                        .store(store)
                        .projectKey(DUMB_PROJECT_KEY)
                        .build();
        Field decryptorServiceField =
                PolicyStoreManagerService.class.getDeclaredField("decryptorService");
        decryptorServiceField.setAccessible(true);
        decryptorServiceField.set(tested, decryptorService);

        // Execute
        tested.doUpdate();

        // Verify: If no update is needed, we should not fetch metadata CSV, not read, and not hit
        // the store.
        verify(sdkCsvFetcher, times(1)).getLastUpdatedUtc(anyString());
        verify(sdkCsvFetcher, never()).fetch(anyString());
        verify(fileReader, never()).readMetadata(any(byte[].class));
        verify(fileReader, never()).readPolicies(any(byte[].class));
        verifyNoInteractions(store);

        System.out.println("[Assert] No further interactions occurred as expected.");
    }

    @Test
    public void doUpdate_updateNeeded_emptyMetadata_thenSecondCallNoRefetch() throws Exception {
        System.out.println(
                "[Test] doUpdate(): metadata newer → update path with empty metadata list");

        // First call: newer time than EPOCH to trigger update.
        OffsetDateTime firstUpdateTime = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1);
        // Second call: return the same timestamp → should NOT trigger another update.
        OffsetDateTime sameAsFirst = firstUpdateTime;

        // Stubbing for both calls:
        // 1) First doUpdate() returns 'firstUpdateTime' (newer → triggers update)
        // 2) Second doUpdate() returns 'sameAsFirst' (same → no update)
        when(sdkCsvFetcher.getLastUpdatedUtc(anyString()))
                .thenReturn(firstUpdateTime) // for first invocation
                .thenReturn(sameAsFirst); // for second invocation

        // When updateDb() runs on the first call, it will fetch metadata CSV and ask the reader to
        // parse it.
        when(sdkCsvFetcher.fetchWithMeta(anyString()))
                // the metadata
                .thenReturn(
                        S3FetchResponseDto.builder()
                                .bytes("scope,updatedAt\n".getBytes(StandardCharsets.UTF_8))
                                .lastUpdatedUtc(OffsetDateTime.now())
                                .build());
        when(sdkCsvFetcher.fetch(anyString()))
                // the scope file
                .thenReturn("scope,updatedAt\n".getBytes(StandardCharsets.UTF_8));
        when(fileReader.readMetadata(any(byte[].class)))
                .thenReturn(
                        Collections.singletonList(new MetadatumModel("scope-1", firstUpdateTime)));

        when(decryptorService.decrypt(any(byte[].class)))
                .thenReturn("scope,updatedAt\n".getBytes(StandardCharsets.UTF_8));

        PolicyStoreManagerService tested =
                PolicyStoreManagerService.builder()
                        .sdkCsvFetcher(sdkCsvFetcher)
                        .fileReader(fileReader)
                        .store(store)
                        .projectKey(DUMB_PROJECT_KEY)
                        .build();
        Field decryptorServiceField =
                PolicyStoreManagerService.class.getDeclaredField("decryptorService");
        decryptorServiceField.setAccessible(true);
        decryptorServiceField.set(tested, decryptorService);

        // Execute first call (should perform: getLastUpdatedAt → get(metadata.csv) → readMetadata)
        tested.doUpdate();

        // Execute second call (should only call getLastUpdatedAt and then stop)
        tested.doUpdate();

        // Verify across both calls:
        // - getLastUpdatedAt: called twice (once per doUpdate())
        verify(sdkCsvFetcher, times(2)).getLastUpdatedUtc(anyString());

        // - Metadata CSV fetched only once (first call only)
        verify(sdkCsvFetcher, times(1)).fetch(anyString());

        // - Reader parses metadata exactly once (empty list case)
        verify(fileReader, times(1)).readMetadata(any(byte[].class));

        verify(fileReader, times(1)).readPolicies(any(byte[].class));

        System.out.println(
                "[Assert] First call performed an update with empty metadata. Second call detected no changes and performed no further work.");
    }
}
