package com.hmg.role.sdk.fetcher.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;

@Getter
public class ProjectEncryptionKeyDto {
    private final String encryptionKey;
    private final String expiredAfter;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ProjectEncryptionKeyDto(
            @JsonProperty("encryptionKey") String encryptionKey,
            @JsonProperty("expiredAfter") String expiredAfter) {
        // needed due to combination of
        // final class + Jackson + Lombok
        // nonsensery
        this.encryptionKey = encryptionKey;
        this.expiredAfter = expiredAfter;
    }

    public boolean expiredAt(ZonedDateTime now) {
        return ZonedDateTime.parse(expiredAfter, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .isBefore(ZonedDateTime.now());
    }
}
