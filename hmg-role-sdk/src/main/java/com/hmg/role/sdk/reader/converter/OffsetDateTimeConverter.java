package com.hmg.role.sdk.reader.converter;

import com.opencsv.bean.AbstractBeanField;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeConverter extends AbstractBeanField<OffsetDateTime, String> {
    @Override
    protected OffsetDateTime convert(String value) {
        return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
