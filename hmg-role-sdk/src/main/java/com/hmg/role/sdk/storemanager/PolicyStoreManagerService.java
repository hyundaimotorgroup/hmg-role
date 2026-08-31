package com.hmg.role.sdk.storemanager;

import com.hmg.role.sdk.common.SdkConstants;
import com.hmg.role.sdk.common.util.CollectionUtils;
import com.hmg.role.sdk.db.MapDbStore;
import com.hmg.role.sdk.fetcher.S3FetcherService;
import com.hmg.role.sdk.fetcher.crypto.DataDecryptionService;
import com.hmg.role.sdk.fetcher.dto.S3FetchResponseDto;
import com.hmg.role.sdk.fetcher.security.AccessKeyApiClient;
import com.hmg.role.sdk.fetcher.security.DecryptorService;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.reader.SdkCsvFileReader;
import com.hmg.role.sdk.reader.model.MetadatumModel;
import com.hmg.role.sdk.reader.model.PolicyItemCsvModel;
import com.hmg.role.sdk.storemanager.models.ScopeMetadataEntry;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PolicyStoreManagerService {
    private final S3FetcherService sdkCsvFetcher;
    private final MapDbStore store;
    private final SdkCsvFileReader fileReader;
    private final AccessKeyApiClient apiClient;
    private final DecryptorService decryptorService;

    private final Map<ScopeModel, ScopeMetadataEntry> scopeEntryTracker;

    private OffsetDateTime lastRecordedMetadataUpdateTime;

    @Builder
    public PolicyStoreManagerService(
            S3FetcherService sdkCsvFetcher,
            SdkCsvFileReader fileReader,
            MapDbStore store,
            AccessKeyApiClient apiClient,
            String projectKey) {
        log.info("Starting hmgRole-SDK Store Manager");
        this.sdkCsvFetcher = sdkCsvFetcher;
        this.store = store;
        this.fileReader = fileReader;
        this.apiClient = apiClient;
        this.decryptorService = new DecryptorService(this.apiClient, new DataDecryptionService());

        this.scopeEntryTracker = new ConcurrentHashMap<>();

        // assume the metadata never present
        // to lazily trigger fetching at first checking
        lastRecordedMetadataUpdateTime = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    }

    public void doUpdate() throws IOException {
        OffsetDateTime fetchedMetadataUpdateTime = getMetadataLastUpdateTime();
        // compare the update timestamp of metadata in S3 against stored
        boolean shouldUpdate =
                checkMetadataTs(fetchedMetadataUpdateTime, lastRecordedMetadataUpdateTime);
        if (shouldUpdate) {
            log.info("Metadata changed. Fetching and updating cache");
            lastRecordedMetadataUpdateTime = updateDb();
        }
    }

    private synchronized OffsetDateTime updateDb() throws IOException {

        // download the new metadata
        S3FetchResponseDto metadataFile =
                sdkCsvFetcher.fetchWithMeta(SdkConstants.METADATA_FILE_NAME);
        byte[] metadataCsv = metadataFile.getBytes();
        OffsetDateTime metadataLastUpdate = metadataFile.getLastUpdatedUtc();

        List<MetadatumModel> metadata = fileReader.readMetadata(metadataCsv);

        // map metadata entry per scope
        Map<ScopeModel, ScopeMetadataEntry> metadataScopeMap =
                metadata.stream()
                        .collect(
                                Collectors.toMap(
                                        Function.identity(), // scopeKey <- file name
                                        Function.identity() // last updated at
                                        ));

        // determine which scopes are new, updated, or gone
        // and update the mapdb accordingly
        Set<ScopeModel> scopesInMetadata = metadataScopeMap.keySet();
        Set<ScopeModel> scopesExisting = scopeEntryTracker.keySet();

        Set<ScopeModel> goneScopes =
                CollectionUtils.getLeftDifference(scopesExisting, scopesInMetadata);
        if (!goneScopes.isEmpty()) {
            log.info("Old scopes were gone in upstream store: {}", goneScopes);
            List<ScopeMetadataEntry> goneScopeMetadata =
                    goneScopes.stream().map(metadataScopeMap::get).collect(Collectors.toList());
            deleteGoneProjectScopes(goneScopeMetadata);
        }

        Set<ScopeModel> persistScopes =
                CollectionUtils.getSetIntersection(goneScopes, scopesInMetadata);
        if (!persistScopes.isEmpty()) {
            log.info("Persisted scopes were updated in upstream store: {}", persistScopes);
            List<ScopeMetadataEntry> persistScopeMetadata =
                    persistScopes.stream().map(metadataScopeMap::get).collect(Collectors.toList());
            updateExistingProjectScopes(persistScopeMetadata);
        }

        Set<ScopeModel> newScopes =
                CollectionUtils.getLeftDifference(scopesInMetadata, scopesExisting);
        if (!newScopes.isEmpty()) {
            log.info("New scopes existed in upstream store: {}", newScopes);
            List<ScopeMetadataEntry> newScopeMetadata =
                    newScopes.stream().map(metadataScopeMap::get).collect(Collectors.toList());
            addNewProjectScopes(newScopeMetadata);
        }

        if (store.getScopeByKeyMap() != null) { // possible on a unit test
            log.info("Scope keys in db is now: {}", store.getScopeByKeyMap().keySet());
        }

        return metadataLastUpdate;
    }

    private void deleteGoneProjectScopes(List<ScopeMetadataEntry> goneScopeMetadata) {
        if (goneScopeMetadata.isEmpty()) {
            log.warn("Gone scope metadata was empty");
            return;
        }
        for (ScopeMetadataEntry scopeMetadatum : goneScopeMetadata) {
            if (scopeMetadatum == null || scopeMetadatum.getScopeKey() == null) {
                log.warn(
                        "Gone scope metadata isn't empty but metadatum is, content: {}",
                        scopeMetadatum);
                return;
            } else {
                deleteProjectScope(scopeMetadatum);
            }
        }
    }

    private void deleteProjectScope(ScopeMetadataEntry scopeMetadatum) {
        store.delete(scopeMetadatum);
        scopeEntryTracker.remove(scopeMetadatum);
    }

    private void updateExistingProjectScopes(List<ScopeMetadataEntry> persistScopeMetadata)
            throws IOException {
        for (ScopeMetadataEntry scopeMetadatum : persistScopeMetadata) {
            byte[] policyCiphertextCsv = sdkCsvFetcher.fetch(scopeMetadatum.getScopeKey());
            byte[] policyCsv = decryptorService.decrypt(policyCiphertextCsv);
            List<PolicyItemCsvModel> policies = fileReader.readPolicies(policyCsv);
            updateExistingProjectScope(scopeMetadatum, policies);
        }
    }

    private void updateExistingProjectScope(
            ScopeMetadataEntry scopeMetadatum, List<PolicyItemCsvModel> policies)
            throws IOException {
        store.update(scopeMetadatum, policies);
        scopeEntryTracker.replace(scopeMetadatum, scopeMetadatum);
    }

    private void addNewProjectScopes(List<ScopeMetadataEntry> newScopeMetadata) throws IOException {
        for (ScopeMetadataEntry scopeMetadatum : newScopeMetadata) {
            byte[] policyCiphertextCsv = sdkCsvFetcher.fetch(scopeMetadatum.getScopeKey());
            byte[] policyCsv = decryptorService.decrypt(policyCiphertextCsv);
            List<PolicyItemCsvModel> policies = fileReader.readPolicies(policyCsv);
            addNewProjectScope(scopeMetadatum, policies);
        }
    }

    private void addNewProjectScope(
            ScopeMetadataEntry projectScopeMetadata, List<PolicyItemCsvModel> policyItems) {
        log.info("Adding new scope metadatum, key: {}", projectScopeMetadata.getScopeKey());
        store.insert(policyItems);
        scopeEntryTracker.put(projectScopeMetadata, projectScopeMetadata);
    }

    private synchronized OffsetDateTime getMetadataLastUpdateTime() {
        OffsetDateTime metadataLastUpdate =
                sdkCsvFetcher.getLastUpdatedUtc(SdkConstants.METADATA_FILE_NAME);
        return metadataLastUpdate;
    }

    private static boolean checkMetadataTs(
            OffsetDateTime fetchedMetadataUpdateTime,
            OffsetDateTime lastRecordedMetadataUpdateTime) {
        return fetchedMetadataUpdateTime.isAfter(lastRecordedMetadataUpdateTime);
    }
}
