package com.hmg.role.common.cdc.dto;

import com.hmg.role.common.cdc.enums.CdcEventType;
import java.nio.file.Path;
import java.time.Clock;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class CdcEventDto extends ApplicationEvent {
    private final CdcEventType eventType;
    private final String project;
    private final String scope;
    private final String fileName;
    private final Path basePath;

    @Builder
    private CdcEventDto(
            CdcEventType eventType, String project, String scope, String fileName, Path basePath) {
        super(tagEvt(eventType, project, scope), Clock.systemDefaultZone());
        this.eventType = eventType;
        this.project = project;
        this.scope = scope;
        this.fileName = fileName.replace("\\", "/"); // curse microsoft and windows
        this.basePath = basePath;
    }

    private static String tagEvt(CdcEventType eventType, String project, String scope) {
        return String.format("%s-%s-%s", eventType, project, scope);
    }
}
