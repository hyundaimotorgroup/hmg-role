package com.hmg.role.common.cdc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CdcStorageProvider {
    S3;

    // add here if it is needed later

    @JsonCreator
    public static CdcStorageProvider of(String value) {
        return CdcStorageProvider.valueOf(value.toUpperCase());
    }
}
