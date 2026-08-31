package com.hmg.role.util.sqlconverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper between a {@link Map}&lt;{@link String}, {@link String}&gt; attribute of an {@link Entity}
 * and a TEXT/VARCHAR column of a table in MySQL. The map will be stored in the database as a JSON
 * string.
 */
@Slf4j
@Converter
public class MapStringToJsonTextConverter
        implements AttributeConverter<Map<String, String>, String> {

    @Autowired private ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting Map to JSON", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            log.error("Error converting JSON String to map", e);
            throw new IllegalArgumentException(e);
        }
    }
}
