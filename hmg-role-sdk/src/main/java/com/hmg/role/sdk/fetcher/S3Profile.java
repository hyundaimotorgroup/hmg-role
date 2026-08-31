package com.hmg.role.sdk.fetcher;

import com.hmg.role.sdk.common.SdkConstants;
import java.net.URI;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class S3Profile {
    String endpoint;

    @Builder.Default String alias = SdkConstants.DEFAULT_S3_BUCKET_PROFILE;
    @NonNull String bucket;
    @NonNull String region;
    @NonNull String accessKey;
    @NonNull String secretKey;
    @NonNull String projectKey;
    @Builder.Default boolean pathStyle = true;
    @Builder.Default boolean secure = true;

    public URI endpointUriOrNull() {
        return endpoint == null ? null : URI.create(endpoint);
    }
}
