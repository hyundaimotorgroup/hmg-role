package com.hmg.role.sdk.config;

import com.hmg.role.sdk.config.enums.SourceType;
import com.hmg.role.sdk.config.enums.StorageType;
import com.hmg.role.sdk.rbac.permission.DataNotFoundStrategy;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
/** Configuration for hmgRole SDK client. */
public class SdkConfig {
    /** If there are any endpoint overrides, e.g. when using MinIO or Alibaba's OSS */
    public String s3Endpoint; // TODO remove

    /** The S3 region of hmgRole policy bucket. Mandatory */
    @NonNull public String s3Region;

    /** The hmgRole S3 bucket name. Mandatory */
    @NonNull public String s3Bucket;

    /** The hmgRole policy S3 bucket access key. Mandatory */
    @NonNull public String s3AccessKey;

    /** The hmgRole policy S3 bucket secret key. Mandatory */
    @NonNull public String s3SecretKey;

    /** Policy sources of the hmgRole client SDK. Supports only S3 bucket for now */
    @Builder.Default public SourceType sourceType = SourceType.REMOTE_S3; // NOSONAR

    /** Storage type of the hmgRole client SDK. Options are MEMORY or FILE. Defaults to MEMORY */
    @Builder.Default public StorageType storageType = StorageType.MEMORY;

    /**
     * What to do by hmgRole SDK if a policy request has no corresponding data. The options are
     * RETURN_PERMISSION_DENY or THROW_EXCEPTION. Defaults to RETURN_PERMISSION_DENY
     */
    @Builder.Default
    public DataNotFoundStrategy dataNotFoundStrategy = DataNotFoundStrategy.RETURN_PERMISSION_DENY;

    /** The client's project key */
    @NonNull public String projectKey;

    /** The client's API key */
    @NonNull public String apiKey;

    /** hmgRole's API endpoint */
    @NonNull public String baseUrlAccessApi;
}
