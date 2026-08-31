package com.hmg.role.common.cdc.eventhandler.components;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Extracts CSV headers from unwrapped Debezium JSON (payload-level columns). */
@Slf4j
@RequiredArgsConstructor
public class CsvHeaderExtractor {

    private static final Set<String> META_FIELDS = Set.of("__op", "__table", "__ts_ms");

    public List<String> extract(JsonNode value) {
        List<String> cols = tryHeadersFromSchemaPayload(value);
        if (!cols.isEmpty()) return cols;

        JsonNode payload = value.get("payload");
        if (payload != null && payload.isObject()) {
            payload.fieldNames()
                    .forEachRemaining(
                            f -> {
                                if (!META_FIELDS.contains(f)) {
                                    cols.add(f);
                                }
                            });
            return cols;
        }
        return new LinkedList<>();
    }

    private List<String> tryHeadersFromSchemaPayload(JsonNode value) {
        List<String> cols = new LinkedList<>();
        try {
            JsonNode schema = value.get("schema");
            if (schema == null || !schema.has("fields")) {
                return new LinkedList<>();
            }
            for (JsonNode level1 : schema.get("fields")) {
                String f1 = level1.path("field").asText();
                if (!"payload".equals(f1)) continue;
                if (!level1.has("fields")) continue;
                for (JsonNode col : level1.get("fields")) {
                    String name = col.path("field").asText();
                    if (name != null && !name.isBlank() && !META_FIELDS.contains(name)) {
                        cols.add(name);
                    }
                }
                return cols;
            }
        } catch (Exception e) {
            log.debug("Header extraction from schema.payload failed: {}", e.getMessage());
        }
        return new LinkedList<>();
    }
}
