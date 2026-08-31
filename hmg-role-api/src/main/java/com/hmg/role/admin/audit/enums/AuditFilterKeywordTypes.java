package com.hmg.role.admin.audit.enums;

import java.util.Arrays;
import java.util.List;

public enum AuditFilterKeywordTypes {
    KEY,
    AUTHOR,
    IP;

    public static List<String> valuesAsList() {
        return Arrays.stream(values())
                .map(AuditFilterKeywordTypes::name)
                .map(String::toLowerCase)
                .toList();
    }
}
