package com.hmg.role.common.cdc.eventhandler.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.hmg.role.common.cdc.utils.CdcUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Builds CSV rows from unwrapped payload according to header order. */
public class CsvRowBuilder {

    public String buildFromPayload(JsonNode payload, List<String> headers) {
        if (payload == null || payload.isNull() || headers == null || headers.isEmpty()) return "";
        List<String> cells = new ArrayList<>(headers.size());
        for (String h : headers) {
            JsonNode v = payload.get(h);
            if (v == null || v.isNull()) {
                cells.add("");
            } else if (v.isNumber() || v.isBoolean()) {
                cells.add(v.asText());
            } else {
                cells.add(CdcUtils.csvEscape(v.asText()));
            }
        }
        boolean allBlank = cells.stream().allMatch(s -> s == null || s.isEmpty());
        if (allBlank) return "";
        return String.join(",", cells);
    }

    public static String toCsvLine(List<String> values) {
        return values.stream().map(CdcUtils::csvEscape).collect(Collectors.joining(","));
    }
}
