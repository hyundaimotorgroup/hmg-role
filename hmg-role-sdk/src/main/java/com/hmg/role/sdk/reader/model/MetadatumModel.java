package com.hmg.role.sdk.reader.model;

import com.hmg.role.sdk.reader.converter.OffsetDateTimeConverter;
import com.hmg.role.sdk.storemanager.models.ScopeMetadataEntry;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class MetadatumModel implements ScopeMetadataEntry {
    @CsvBindByName(column = "file_name")
    String fileName;

    // OpenCSV default timestamp column format is the compressed ISO 8601 (ones without dashes)
    @CsvCustomBindByName(column = "last_updated_at", converter = OffsetDateTimeConverter.class)
    OffsetDateTime lastUpdatedAt;

    @Override
    public String getScopeKey() {
        return fileName;
    }
}
