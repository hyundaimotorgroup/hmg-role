package com.hmg.role.sdk;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.hmg.role.sdk.common.enums.Effect;
import com.hmg.role.sdk.config.enums.SourceType;
import com.hmg.role.sdk.config.enums.StorageType;
import com.hmg.role.sdk.fetcher.S3FetcherService;
import com.hmg.role.sdk.fetcher.crypto.DataDecryptionService;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;
import com.hmg.role.sdk.fetcher.dto.S3FetchResponseDto;
import com.hmg.role.sdk.fetcher.security.AccessKeyApiClient;
import com.hmg.role.sdk.fetcher.security.DecryptorService;
import com.hmg.role.sdk.rbac.permission.DataNotFoundStrategy;
import com.hmg.role.sdk.rbac.permission.PermissionService;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByRoleDto;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponse;
import com.hmg.role.sdk.storemanager.PolicyStoreManagerService;
import com.hmg.role.sdk.testcommon.TestUtils;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@Tags({@Tag("unitTest"), @Tag("smallTest"), @Tag("sdk")})
public class PermissionServiceFactoryIntegrationTest {
    // integration tests with provided DB and reader
    private AutoCloseable mocksCloseable;

    @Mock private S3FetcherService mockS3FetcherService;
    @Mock private AccessKeyApiClient mockAccessKeyApiClient;
    @Mock private DataDecryptionService mockDataDecryptor;

    // mock encryption process
    // since it's not maintainable to implement AES256+GCM+cryptothingamajig in a test
    // maybe later it's easier to encrypt the test data but eh
    private Map<String, byte[]> dumbEncryptionData;

    public PermissionServiceFactoryIntegrationTest() {}

    @BeforeEach
    void setUpEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        dumbEncryptionData = new HashMap<>(4);
    }

    @AfterEach
    void tearDownEach() throws Exception {
        Mockito.reset(mockS3FetcherService, mockAccessKeyApiClient, mockDataDecryptor);
        mocksCloseable.close();
    }

    @Test
    void getRbacPermissionService() throws Exception {
        String policyItemsFileName = "csv/policy-1-denormed-integration.csv";
        String metadataFileName = "csv/metadata-integration.csv";

        byte[] dumbEncryptionKey = UUID.randomUUID().toString().getBytes();

        when(mockS3FetcherService.fetch(anyString()))
                .then(
                        (p) -> {
                            String fileName = p.getArgument(0, String.class);
                            byte[] ans;
                            if (fileName.contains("metadata")) {
                                ans = TestUtils.readFile(metadataFileName);
                            } else {
                                ans = TestUtils.readFile(policyItemsFileName);
                            }
                            System.out.println(
                                    "downloaded: "
                                            + fileName
                                            + " put as: "
                                            + String.valueOf(Arrays.hashCode(ans)));
                            dumbEncryptionData.put(String.valueOf(Arrays.hashCode(ans)), ans);
                            return ans;
                        });
        when(mockS3FetcherService.fetchWithMeta(anyString()))
                .then(
                        (p) -> {
                            String fileName = p.getArgument(0, String.class);
                            if (fileName.contains("metadata")) {
                                return S3FetchResponseDto.builder()
                                        .bytes(TestUtils.readFile(metadataFileName))
                                        .lastUpdatedUtc(OffsetDateTime.now())
                                        .build();
                            } else {
                                return S3FetchResponseDto.builder()
                                        .bytes(TestUtils.readFile(policyItemsFileName))
                                        .lastUpdatedUtc(OffsetDateTime.now())
                                        .build();
                            }
                        });
        when(mockS3FetcherService.getLastUpdatedUtc(anyString())).thenReturn(OffsetDateTime.now());

        when(mockDataDecryptor.decrypt(any(), any()))
                .then(
                        a -> {
                            byte[] ct = a.getArgument(0, byte[].class);
                            String key = String.valueOf(Arrays.hashCode(ct));
                            System.out.println("downloading, get: " + key);
                            return dumbEncryptionData.get(key);
                        });

        String hmgRoleBaseUrl = "http://gmarket.co.kr";
        String apiKey = "api-key";

        PermissionServiceFactory testedFactory =
                new PermissionServiceFactory(
                        SourceType.REMOTE_S3,
                        StorageType.MEMORY,
                        () -> mockS3FetcherService,
                        DataNotFoundStrategy.RETURN_PERMISSION_DENY,
                        hmgRoleBaseUrl,
                        apiKey,
                        "Project 1");

        // force mock encryption
        mockApiAndEncryption(dumbEncryptionKey, testedFactory);

        PermissionService tested = testedFactory.getRbacPermissionService();

        PermissionFlatRequestByRoleDto expectedAllowReq =
                PermissionFlatRequestByRoleDto.builder()
                        .roleKey("Finance Manager")
                        .scopeKey("Scope 1")
                        .resourceTypeKey("Finance Document")
                        .actionName("view")
                        .build();

        System.out.println("request: " + expectedAllowReq.toString());
        Collection<? extends PermissionFlatResponse> resAllowed =
                tested.getPermissionsFlattened(expectedAllowReq);
        System.out.println("response: ");
        for (PermissionFlatResponse resElem : resAllowed) {
            System.out.println(resElem);
            Assertions.assertEquals(Effect.ALLOW, resElem.getEffect());
        }

        PermissionFlatRequestByRoleDto expectedDenyReq =
                PermissionFlatRequestByRoleDto.builder()
                        .roleKey("Finance Member")
                        .scopeKey("Scope 1")
                        .resourceTypeKey("Finance Document")
                        .actionName("modify")
                        .build();
        System.out.println("request: " + expectedDenyReq.toString());
        Collection<? extends PermissionFlatResponse> resDenied =
                tested.getPermissionsFlattened(expectedDenyReq);
        System.out.println("response: ");
        for (PermissionFlatResponse resElem : resDenied) {
            System.out.println(resElem);
            Assertions.assertEquals(Effect.DENY, resElem.getEffect());
        }
    }

    private void mockApiAndEncryption(
            byte[] dumbEncryptionKey, PermissionServiceFactory testedFactory)
            throws NoSuchFieldException, IllegalAccessException {
        AccessKeyApiClient dumbApiClient =
                () -> {
                    System.out.println("fetchDecryptionKey");
                    return new ProjectEncryptionKeyDto(
                            Base64.getEncoder().encodeToString(dumbEncryptionKey),
                            TestUtils.END_OF_TIME_STR);
                };
        Field apiClientField = PermissionServiceFactory.class.getDeclaredField("apiClient");
        apiClientField.setAccessible(true);
        apiClientField.set(testedFactory, dumbApiClient);
        Field storeManagerField = testedFactory.getClass().getDeclaredField("storeManager");
        storeManagerField.setAccessible(true);
        PolicyStoreManagerService storeManager =
                (PolicyStoreManagerService) storeManagerField.get(testedFactory);
        Field decryptorServiceField = storeManager.getClass().getDeclaredField("decryptorService");
        decryptorServiceField.setAccessible(true);
        DecryptorService decryptorService =
                (DecryptorService) decryptorServiceField.get(storeManager);
        Field dataDecryptorField = decryptorService.getClass().getDeclaredField("dataDecryptor");
        dataDecryptorField.setAccessible(true);
        dataDecryptorField.set(decryptorService, mockDataDecryptor);
        Field accessApiClientField =
                decryptorService.getClass().getDeclaredField("accessKeyApiClient");
        accessApiClientField.setAccessible(true);
        accessApiClientField.set(decryptorService, dumbApiClient);
    }
}
