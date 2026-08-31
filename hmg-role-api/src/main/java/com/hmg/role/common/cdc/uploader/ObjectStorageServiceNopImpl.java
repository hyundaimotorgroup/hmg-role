package com.hmg.role.common.cdc.uploader;

import com.hmg.role.common.cdc.dto.CdcEventDto;
import com.hmg.role.common.cdc.uploader.interfaces.ObjectStorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("!T(java.lang.Boolean).valueOf('${cdc.enabled:false}')")
public class ObjectStorageServiceNopImpl implements ObjectStorageService {
    // NOP-implementation when CDC is disabled

    @Override
    public void pushChanges(CdcEventDto event) {}

    @Override
    public void reencrypt(String projectKey, byte[] oldKey, byte[] newKey) {}

    @Override
    public void deleteProjectDirectory(String projectKey) {}
}
