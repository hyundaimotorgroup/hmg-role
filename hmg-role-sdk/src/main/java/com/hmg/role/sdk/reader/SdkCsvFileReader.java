package com.hmg.role.sdk.reader;

import com.hmg.role.sdk.reader.model.MetadatumModel;
import com.hmg.role.sdk.reader.model.PolicyItemCsvModel;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SdkCsvFileReader {

    /** The column separator in the CSV. Defaults to comma (',') */
    @Builder.Default public final Character separator = ',';

    /** Whether to ignore leading space in columns. Defaults to true */
    @Builder.Default public final Boolean ignoreLeadingWhiteSpace = true;

    /** Whether to ignore empty rows in the file. Defaults to true */
    @Builder.Default public final Boolean ignoreEmptyLine = true;

    /**
     * Reads a CSV file policy. For now it is in the format of denormalized policy actions and
     * effects
     *
     * @param policyCsvFile the CSV file
     * @return List of policy models
     * @throws IOException when something is wrong when reading the CSV file
     */
    public List<PolicyItemCsvModel> readPolicies(byte[] policyCsvFile) throws IOException {
        return readPolicyModelCsvFile(policyCsvFile, PolicyItemCsvModel.class);
    }

    public List<MetadatumModel> readMetadata(byte[] metadataCsvFile) throws IOException {
        return readPolicyModelCsvFile(metadataCsvFile, MetadatumModel.class);
    }

    private <T> List<T> readPolicyModelCsvFile(byte[] csvFile, Class<T> modelClass)
            throws IOException {

        // could possibly be generified but for now this will do
        try (Reader reader =
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvFile)))) {
            CsvToBean<T> csvToBean =
                    (new CsvToBeanBuilder<T>(reader))
                            // this is the only CSV data model class
                            // it should not be configurable
                            .withType(modelClass)
                            .withIgnoreLeadingWhiteSpace(ignoreLeadingWhiteSpace)
                            .withIgnoreEmptyLine(ignoreEmptyLine)
                            // just in case there are possibilities of other locales using ';'
                            .withSeparator(separator)
                            .build();

            return csvToBean.parse();
        }
    }
}
