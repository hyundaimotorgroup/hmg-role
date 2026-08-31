package com.hmg.role.sdk.fetcher;

import com.hmg.role.sdk.fetcher.dto.S3FetchResponseDto;
import java.time.OffsetDateTime;

public interface S3FetcherService {
    /** download object bytes */
    byte[] fetch(String objectPath);

    OffsetDateTime getLastUpdatedUtc(String objectPath);

    S3FetchResponseDto fetchWithMeta(String objectPath);
}
