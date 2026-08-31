package com.hmg.role.common.cdc.uploader.interfaces;

import com.hmg.role.common.cdc.dto.CdcEventDto;

public interface ObjectStorageService {
    void pushChanges(CdcEventDto event);

    void reencrypt(String projectKey, byte[] oldKey, byte[] newKey);

    void deleteProjectDirectory(String projectKey);
}
