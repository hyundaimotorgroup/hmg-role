package com.hmg.role.sdk.reader;

import com.hmg.role.sdk.reader.model.PolicyItemCsvModel;
import com.hmg.role.sdk.testcommon.TestUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

@Tags({@Tag("unitTest"), @Tag("smallTest"), @Tag("sdk")})
class SdkCsvFileReaderTest {
    public SdkCsvFileReaderTest() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeEach
    void setUpEach() {}

    @AfterEach
    void tearDownEach() {}

    @Test
    void readPolicyModelCsvFile() throws IOException {
        SdkCsvFileReader tested = SdkCsvFileReader.builder().build();
        List<PolicyItemCsvModel> actual =
                tested.readPolicies(TestUtils.readFile("csv/policy-1-denormed.csv"));

        List<PolicyItemCsvModel> expected = generatePolicyItems();

        actual.sort(Comparator.comparing(PolicyItemCsvModel::toString));
        expected.sort(Comparator.comparing(PolicyItemCsvModel::toString));
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertEquals(expected.get(i), actual.get(i));
        }
    }

    private static List<PolicyItemCsvModel> generatePolicyItems() {
        List<PolicyItemCsvModel> res = new ArrayList<>();
        res.add(
                new PolicyItemCsvModel(
                        "Finance Manager",
                        "Scope 1",
                        "view",
                        "Finance Document",
                        "ALLOW",
                        "Project 1",
                        "User 1"));
        res.add(
                new PolicyItemCsvModel(
                        "Finance Manager",
                        "Scope 1",
                        "modify",
                        "Finance Document",
                        "ALLOW",
                        "Project 1",
                        "User 1"));
        res.add(
                new PolicyItemCsvModel(
                        "Finance Member",
                        "Scope 1",
                        "view",
                        "Finance Document",
                        "ALLOW",
                        "Project 1",
                        "User 1"));
        return res;
    }
}
