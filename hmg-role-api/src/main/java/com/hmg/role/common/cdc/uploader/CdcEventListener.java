package com.hmg.role.common.cdc.uploader;

import com.hmg.role.common.cdc.dto.CdcEventDto;
import com.hmg.role.common.cdc.uploader.interfaces.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CdcEventListener {

    private final ObjectStorageService objectStorageService;

    @EventListener(
            condition =
                    "#event.eventType == T(com.hmg.role.common.cdc.enums.CdcEventType).CDC_WRITING_END")
    public void handleWritingEndEvent(CdcEventDto event) {
        objectStorageService.pushChanges(event);
    }
}
