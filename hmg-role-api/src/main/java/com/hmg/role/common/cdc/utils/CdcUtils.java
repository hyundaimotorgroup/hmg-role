package com.hmg.role.common.cdc.utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CdcUtils {
    public static String csvEscape(String s) {
        if (s == null) return "";
        boolean needsQuote =
                s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return needsQuote ? "\"" + v + "\"" : v;
    }

    public static List<String> buildCdcDeleteToken(JsonNode payload) {
        List<String> tokens = new ArrayList<>();
        addIfPresent(tokens, payload, "policy_item_key");
        addIfPresent(tokens, payload, "policy_key");
        addIfPresent(tokens, payload, "role_key");
        addIfPresent(tokens, payload, "resource_type_key");
        addIfPresent(tokens, payload, "action_name");
        return tokens;
    }

    private static void addIfPresent(List<String> tokens, JsonNode payload, String field) {
        if (payload != null && payload.hasNonNull(field)) {
            String v = payload.path(field).asText();
            if (v != null && !v.isBlank()) tokens.add(v);
        }
    }

    public static String toCsvLine(List<String> values) {
        return values.stream().map(CdcUtils::csvEscape).collect(Collectors.joining(","));
    }
}
