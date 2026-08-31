package com.hmg.role.sdk.fetcher.dto;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class S3FetchResponseDto {
    byte[] bytes;
    OffsetDateTime lastUpdatedUtc;
    String eTag;
    Long contentLength;
    String contentType;
}
