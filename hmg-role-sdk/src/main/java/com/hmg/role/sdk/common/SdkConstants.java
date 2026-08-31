package com.hmg.role.sdk.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SdkConstants {
    public static final String METADATA_FILE_NAME = "metadata.csv";
    public static final String DEFAULT_SCOPE_KEY = "default_scope";
    public static final String DEFAULT_S3_BUCKET_PROFILE = "default";
}
