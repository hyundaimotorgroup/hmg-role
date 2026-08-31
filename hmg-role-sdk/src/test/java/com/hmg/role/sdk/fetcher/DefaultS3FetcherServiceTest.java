package com.hmg.role.sdk.fetcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hmg.role.sdk.fetcher.dto.S3FetchResponseDto;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

@ExtendWith(MockitoExtension.class)
class DefaultS3FetcherServiceTest {

    @Mock private S3Client mockS3Client;

    private S3Profile profile;

    private String testProjectKey = "test-project-key";

    public DefaultS3FetcherServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void setUp() {
        profile =
                S3Profile.builder()
                        .alias("minio-local")
                        .bucket("demo-bucket")
                        .region("us-east-1")
                        .accessKey("minioadmin")
                        .secretKey("minioadmin")
                        .endpoint("http://localhost:9000")
                        .pathStyle(true)
                        .secure(false)
                        .projectKey(testProjectKey)
                        .build();
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(mockS3Client);
    }

    /** Create service via reflection to inject a mocked s3 client */
    private DefaultS3FetcherService newService() throws Exception {
        Constructor<DefaultS3FetcherService> ctor =
                DefaultS3FetcherService.class.getDeclaredConstructor(
                        S3Profile.class, S3Client.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(profile, mockS3Client, testProjectKey);
    }

    @Test
    void fetchAndReturnBytes() throws Exception {
        DefaultS3FetcherService service = newService();

        byte[] payload = "role_key,scope_key\nadmin,global\n".getBytes(StandardCharsets.UTF_8);
        // Mock ResponseBytes<GetObjectResponse>

        GetObjectResponse resp =
                GetObjectResponse.builder()
                        .contentType("text/csv")
                        .contentLength((long) payload.length)
                        .build();

        // Build a real ResponseBytes (NO mocking final class)
        ResponseBytes<GetObjectResponse> rb = ResponseBytes.fromByteArray(resp, payload);

        // Stub s3.getObjectAsBytes
        when(mockS3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(rb);

        byte[] result = service.fetch("policies.csv");

        assertNotNull(result);
        assertArrayEquals(payload, result);

        verify(mockS3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
    }

    @Test
    void getLastUpdatedUtcAndHeadObjectAndReturnUtcZonedDateTime() throws Exception {
        DefaultS3FetcherService service = newService();

        Instant lastModified = Instant.parse("2025-01-01T00:00:00Z");
        HeadObjectResponse head =
                HeadObjectResponse.builder()
                        .lastModified(lastModified)
                        .contentLength(123L)
                        .contentType("text/csv")
                        .eTag("\"abc123\"")
                        .build();

        when(mockS3Client.headObject(any(HeadObjectRequest.class))).thenReturn(head);

        OffsetDateTime zdt = service.getLastUpdatedUtc("policies.csv");

        assertEquals(OffsetDateTime.ofInstant(lastModified, ZoneOffset.UTC), zdt);

        verify(mockS3Client, times(1)).headObject(any(HeadObjectRequest.class));
    }

    @Test
    void fetchWithMetaAndReturnBytesAndMetadata() throws Exception {

        DefaultS3FetcherService svc = newService();

        byte[] payload =
                "role_key,scope_key\nadmin,global\n"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Instant lastMod = Instant.parse("2025-02-02T12:34:56Z");

        // Build a real response metadata object
        GetObjectResponse resp =
                GetObjectResponse.builder()
                        .lastModified(lastMod)
                        .eTag("\"etag-xyz\"")
                        .contentLength((long) payload.length)
                        .contentType("text/csv")
                        .build();

        ResponseBytes<GetObjectResponse> rb = ResponseBytes.fromByteArray(resp, payload);
        when(mockS3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(rb);

        S3FetchResponseDto result = svc.fetchWithMeta("policies.csv");

        assertNotNull(result);
        assertArrayEquals(payload, result.getBytes());
        assertEquals(OffsetDateTime.ofInstant(lastMod, ZoneOffset.UTC), result.getLastUpdatedUtc());
        assertEquals("\"etag-xyz\"", result.getETag());
        assertEquals(payload.length, result.getContentLength().intValue());
        assertEquals("text/csv", result.getContentType());

        verify(mockS3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
    }
}
