package com.hmg.role.sdk;

import com.hmg.role.sdk.config.SdkConfig;
import com.hmg.role.sdk.config.enums.SourceType;
import com.hmg.role.sdk.config.enums.StorageType;
import com.hmg.role.sdk.db.MapDbStore;
import com.hmg.role.sdk.db.interfaces.UpdateHook;
import com.hmg.role.sdk.db.provider.impl.PolicyItemProviderImpl;
import com.hmg.role.sdk.db.provider.impl.ResourceActionProviderImpl;
import com.hmg.role.sdk.db.provider.impl.RoleProviderImpl;
import com.hmg.role.sdk.db.provider.impl.ScopeProviderImpl;
import com.hmg.role.sdk.fetcher.S3FetcherFactory;
import com.hmg.role.sdk.fetcher.S3FetcherService;
import com.hmg.role.sdk.fetcher.S3Profile;
import com.hmg.role.sdk.fetcher.security.AccessKeyApiClient;
import com.hmg.role.sdk.fetcher.security.AccessKeyApiClientImpl;
import com.hmg.role.sdk.rbac.permission.*;
import com.hmg.role.sdk.rbac.permission.DataExistenceValidator;
import com.hmg.role.sdk.rbac.permission.DataExistenceValidatorImpl;
import com.hmg.role.sdk.rbac.permission.DataNotFoundStrategy;
import com.hmg.role.sdk.rbac.permission.PermissionService;
import com.hmg.role.sdk.rbac.permission.PermissionServiceImpl;
import com.hmg.role.sdk.rbac.permission.spi.PolicyItemProvider;
import com.hmg.role.sdk.rbac.permission.spi.ResourceActionProvider;
import com.hmg.role.sdk.rbac.permission.spi.RoleProvider;
import com.hmg.role.sdk.rbac.permission.spi.ScopeProvider;
import com.hmg.role.sdk.reader.SdkCsvFileReader;
import com.hmg.role.sdk.storemanager.PolicyStoreManagerService;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DBMaker;

@Slf4j
public final class PermissionServiceFactory {

    private final RoleProvider roleProvider;
    private final ScopeProvider scopeProvider;
    private final PolicyItemProvider policyItemProvider;
    private final ResourceActionProvider resourceActionProvider;

    private final DataExistenceValidator dataExistenceValidator;
    private final DataNotFoundStrategy dataNotFoundStrategy;
    private final PolicyStoreManagerService storeManager;
    private final SdkCsvFileReader reader;
    private final MapDbStore store;
    private final S3FetcherService fetcher;
    private final AccessKeyApiClient apiClient;

    private final String projectKey;

    /**
     * Create a new instance of a PermissionServiceFactory based on the given {@code config}.
     *
     * @param config Configuration of the SDK.
     * @return a PermissionServiceFactory that could produce an instance of an {@link
     *     PermissionService}
     */
    public static PermissionServiceFactory create(SdkConfig config) {
        String s3Endpoint = config.s3Endpoint;
        String s3Region = config.s3Region;
        String s3Bucket = config.s3Bucket;
        String s3AccessKey = config.s3AccessKey;
        String s3SecretKey = config.s3SecretKey;

        String hmgRoleBaseUrl = config.baseUrlAccessApi;
        String projectKey = config.projectKey;
        String apiKey = config.apiKey;

        S3Profile s3Profile =
                S3Profile.builder()
                        .endpoint(s3Endpoint)
                        .region(s3Region)
                        .bucket(s3Bucket)
                        .accessKey(s3AccessKey)
                        .secretKey(s3SecretKey)
                        .pathStyle(true)
                        .projectKey(projectKey)
                        .build();
        S3FetcherFactory s3Factory = S3FetcherFactory.builder().profile(s3Profile).build();
        Supplier<S3FetcherService> fetcherSupplier = s3Factory::get;
        return new PermissionServiceFactory(
                config.sourceType,
                config.storageType,
                fetcherSupplier,
                config.dataNotFoundStrategy,
                hmgRoleBaseUrl,
                apiKey,
                projectKey);
    }

    PermissionServiceFactory(
            SourceType sourceType,
            StorageType storageType,
            Supplier<S3FetcherService> fetcherSupplier,
            DataNotFoundStrategy dataNotFoundStrategy,
            String hmgRoleBaseUrl,
            String apiKey,
            String projectKey) {
        // should this be configurable? Open for suggestions
        DBMaker.Maker dbMaker = configDb(storageType);

        // should this be configurable? Open for suggestions
        reader = SdkCsvFileReader.builder().build();

        store = MapDbStore.builder().dbMaker(dbMaker).build();

        fetcher = fetcherSupplier.get();

        // TODO create its own factory
        this.projectKey = projectKey;
        this.apiClient = new AccessKeyApiClientImpl(hmgRoleBaseUrl, apiKey);

        storeManager =
                PolicyStoreManagerService.builder()
                        .sdkCsvFetcher(fetcher)
                        .fileReader(reader)
                        .store(store)
                        .apiClient(apiClient)
                        .projectKey(projectKey)
                        .build();

        UpdateHook hook = this::doStoreUpdate;

        // TODO make all these configurable from the config class
        roleProvider = RoleProviderImpl.builder().mapDbStore(store).updateHook(hook).build();
        scopeProvider = ScopeProviderImpl.builder().mapDbStore(store).updateHook(hook).build();
        policyItemProvider =
                PolicyItemProviderImpl.builder().mapDbStore(store).updateHook(hook).build();
        resourceActionProvider =
                ResourceActionProviderImpl.builder().mapDbStore(store).updateHook(hook).build();

        this.dataNotFoundStrategy =
                Optional.ofNullable(dataNotFoundStrategy)
                        .orElse(DataNotFoundStrategy.RETURN_PERMISSION_DENY);

        dataExistenceValidator =
                new DataExistenceValidatorImpl(roleProvider, scopeProvider, resourceActionProvider);
    }

    /**
     * Get an instance of an RBAC {@link PermissionService}.
     *
     * @return an instance of an RBAC {@link PermissionService} based on the given configuration.
     */
    public PermissionService getRbacPermissionService() {
        return PermissionServiceImpl.builder()
                .dataExistenceValidator(dataExistenceValidator)
                .dataNotFoundStrategy(dataNotFoundStrategy)
                .roleProvider(roleProvider)
                .scopeProvider(scopeProvider)
                .resourceActionProvider(resourceActionProvider)
                .policyItemProvider(policyItemProvider)
                .build();
    }

    private void doStoreUpdate() {
        try {
            storeManager.doUpdate();
        } catch (IOException e) {
            log.error("failed to update store, reason: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static DBMaker.Maker configDb(StorageType storageType) {
        DBMaker.Maker dbMaker;
        switch (storageType) {
            case MEMORY:
                dbMaker = DBMaker.memoryDB();
                break;
            case FILE:
                dbMaker = DBMaker.tempFileDB();
                break;
            default:
                throw new IllegalArgumentException("Invalid storage type: " + storageType);
        }

        dbMaker = dbMaker.transactionEnable();

        return dbMaker;
    }
}
