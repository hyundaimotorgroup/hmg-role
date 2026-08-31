package com.hmg.role.sdk.fetcher;

import com.hmg.role.sdk.common.util.Utils;
import com.hmg.role.sdk.fetcher.dto.S3FetchResponseDto;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Getter
@RequiredArgsConstructor
final class DefaultS3FetcherService implements S3FetcherService, AutoCloseable {

    private final S3Profile profile;
    private final S3Client s3Client;

    private final String projectKey;

    public static DefaultS3FetcherService of(S3Profile profile) {
        AwsBasicCredentials awsCreds =
                AwsBasicCredentials.create(profile.getAccessKey(), profile.getSecretKey());

        StaticCredentialsProvider credsProvider = StaticCredentialsProvider.create(awsCreds);

        S3Configuration s3cfg =
                S3Configuration.builder().pathStyleAccessEnabled(profile.isPathStyle()).build();

        S3ClientBuilder builder =
                S3Client.builder()
                        .region(Region.of(profile.getRegion()))
                        .credentialsProvider(credsProvider)
                        .serviceConfiguration(s3cfg);

        URI endpoint = profile.endpointUriOrNull();
        if (endpoint != null) {
            builder = builder.endpointOverride(endpoint);
        }

        String projectKey = Utils.sha256(profile.getProjectKey());

        S3Client client = builder.build();
        return new DefaultS3FetcherService(profile, client, projectKey);
    }

    @Override
    public byte[] fetch(String objectPath) {
        try {
            ResponseBytes<GetObjectResponse> bytes =
                    s3Client.getObjectAsBytes(
                            GetObjectRequest.builder()
                                    .bucket(profile.getBucket())
                                    .key(projectKey + "/" + objectPath)
                                    .build());
            return bytes.asByteArray();

        } catch (NoSuchKeyException e) {
            throw new RuntimeException("Object not found: " + objectPath, e);
        } catch (S3Exception e) {
            throw new RuntimeException(
                    "S3 error fetching " + objectPath + ": " + e.awsErrorDetails().errorMessage(),
                    e);
        }
    }

    @Override
    public OffsetDateTime getLastUpdatedUtc(String objectPath) {
        try {
            HeadObjectResponse head =
                    s3Client.headObject(
                            HeadObjectRequest.builder()
                                    .bucket(profile.getBucket())
                                    .key(projectKey + "/" + objectPath)
                                    .build());
            return OffsetDateTime.ofInstant(head.lastModified(), ZoneOffset.UTC);
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("Object not found: " + objectPath, e);
        } catch (S3Exception e) {
            throw new RuntimeException(
                    "S3 error retrieving metadata for "
                            + objectPath
                            + ": "
                            + e.awsErrorDetails().errorMessage(),
                    e);
        }
    }

    @Override
    public S3FetchResponseDto fetchWithMeta(String objectPath) {
        try {
            ResponseBytes<GetObjectResponse> rb =
                    s3Client.getObjectAsBytes(
                            GetObjectRequest.builder()
                                    .bucket(profile.getBucket())
                                    .key(projectKey + "/" + objectPath)
                                    .build());
            GetObjectResponse resp = rb.response();

            return S3FetchResponseDto.builder()
                    .bytes(rb.asByteArray())
                    .lastUpdatedUtc(OffsetDateTime.ofInstant(resp.lastModified(), ZoneOffset.UTC))
                    .eTag(resp.eTag())
                    .contentLength(resp.contentLength())
                    .contentType(resp.contentType())
                    .build();
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("Object not found: " + objectPath, e);
        } catch (S3Exception e) {
            throw new RuntimeException(
                    "S3 error fetching " + objectPath + ": " + e.awsErrorDetails().errorMessage(),
                    e);
        }
    }

    @Override
    public void close() {
        try {
            s3Client.close();
        } catch (Exception ignored) {
        }
    }
}
