package com.hmg.role.sdk.fetcher.security;

import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;

public interface AccessKeyApiClient {
    ProjectEncryptionKeyDto fetchDecryptionKey() throws Exception;
}
