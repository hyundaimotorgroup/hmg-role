package com.hmg.role.common.cdc.uploader.interfaces;

import com.hmg.role.common.cdc.dto.CdcEventDto;

public interface UploaderService {
    void pushChanges(CdcEventDto event);
}
