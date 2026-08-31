package com.hmg.role.sdk.db;

import com.hmg.role.sdk.db.keys.PolicyItemMapDbKey;
import com.hmg.role.sdk.db.keys.RoleByUserScopeMapDbKey;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.reader.model.PolicyItemCsvModel;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@Tags({@Tag("unitTest"), @Tag("smallTest"), @Tag("sdk")})
public class MapDbStoreTest {
    private static final int DEFAULT_POLICY_ITEM_SIZE = 10;
    // contains sample columns only, the data is randomly generated later
    public static final String TEST_POLICY_CSV_FILE_NAME = "csv/policy-1-denormed.csv";

    private DBMaker.Maker dbMaker;

    public MapDbStoreTest() {
        MockitoAnnotations.initMocks(this);
        setUpEach();
    }

    @BeforeEach
    public void setUpEach() {
        dbMaker = DBMaker.memoryDB().transactionEnable();
    }

    @AfterEach
    public void tearDownEach() {
        Mockito.reset();
    }

    @Test
    void testParseFile() throws IOException {
        System.out.println("testParseFile");

        List<PolicyItemCsvModel> testPolicyData = getTestPolicyData(10);
        MapDbStore tested = MapDbStore.builder().dbMaker(dbMaker).build();
        tested.insert(testPolicyData);

        BTreeMap<PolicyItemMapDbKey, PolicyItemCsvModel> policies = tested.getPolicyItemByKeyMap();

        System.out.println(String.join("\n", policies.getKeys().toString()));
        System.out.println(
                policies.getKeys().stream()
                        .map(k -> String.format("key: %s, value: %s", k, policies.get(k)))
                        .collect(Collectors.joining("\n")));
    }

    @Test
    void testGetPolicies() throws IOException {
        System.out.println("testGetPolicies");

        List<PolicyItemCsvModel> testPolicies = getTestPolicyData(10);
        MapDbStore tested = MapDbStore.builder().dbMaker(dbMaker).build();
        tested.insert(testPolicies);

        BTreeMap<PolicyItemMapDbKey, PolicyItemCsvModel> policies = tested.getPolicyItemByKeyMap();
        policies.keySet()
                .forEach(k -> System.out.printf("key: %s, value: %s%n", k, policies.get(k)));

        Map<PolicyItemMapDbKey, PolicyItemCsvModel> polMap =
                testPolicies.stream()
                        .collect(Collectors.toMap(PolicyItemMapDbKey::new, Function.identity()));

        polMap.keySet()
                .forEach(
                        p -> {
                            System.out.println("querying: " + p.toString());
                            Assertions.assertTrue(policies.containsKey(p));
                            System.out.println(policies.get(p));
                        });

        PolicyItemMapDbKey k;
        do {
            k =
                    new PolicyItemMapDbKey(
                            UUID.randomUUID().toString(),
                            UUID.randomUUID().toString(),
                            UUID.randomUUID().toString(),
                            UUID.randomUUID().toString());
        } while (policies.containsKey(k));

        System.out.println("querying non-existent: " + k);
        Assertions.assertFalse(policies.containsKey(k));
    }

    @Test
    void testGetRolesByKey() throws IOException {
        System.out.println("testGetRolesByKey");

        List<PolicyItemCsvModel> testPolicies = getTestPolicyData(10);
        MapDbStore tested = MapDbStore.builder().dbMaker(dbMaker).build();
        tested.insert(testPolicies);

        List<String> roleKeys =
                testPolicies.stream()
                        .map(RoleModel::getRoleKey)
                        .distinct()
                        .collect(Collectors.toList());

        HTreeMap<String, RoleModel> roles = tested.getRolesByKeyMap();
        roles.keySet().forEach(k -> System.out.printf("key: %s, value: %s%n", k, roles.get(k)));
    }

    @Test
    void testGetRolesByUserScopeKey() throws IOException {
        System.out.println("testGetRolesByUserScopeKey");

        List<PolicyItemCsvModel> testPolicies = getTestPolicyData(10);

        for (int i = 0; i < testPolicies.size() / 2; i++) {
            testPolicies.get(i).setUserKey("Bon Jovi");
            testPolicies.get(i).setScopeKey("Vocalist");
        }

        for (int i = testPolicies.size() / 4; i < testPolicies.size() / 2; i++) {
            testPolicies.get(i).setScopeKey("Duo-Vocalist");
        }

        MapDbStore tested = MapDbStore.builder().dbMaker(dbMaker).build();
        tested.insert(testPolicies);

        List<RoleByUserScopeMapDbKey> userScopeKeys =
                testPolicies.stream()
                        .map(RoleByUserScopeMapDbKey::new)
                        .distinct()
                        .collect(Collectors.toList());

        System.out.println("userScopeKeys: " + userScopeKeys);

        BTreeMap<RoleByUserScopeMapDbKey, RoleModel> roles = tested.getRolesByUserScopeKeyMap();
        roles.keySet().forEach(k -> System.out.printf("key: %s, value: %s%n", k, roles.get(k)));
    }

    @Test
    void testGetScopesByKey() throws IOException {
        System.out.println("testGetScopesByKey");

        List<PolicyItemCsvModel> testPolicies = getTestPolicyData(10);
        for (int i = testPolicies.size() / 2; i < testPolicies.size(); i++) {
            testPolicies.get(i).setScopeKey("covfefe");
        }

        MapDbStore tested = MapDbStore.builder().dbMaker(dbMaker).build();
        tested.insert(testPolicies);

        List<String> scopeKeys =
                testPolicies.stream()
                        .map(PolicyItemCsvModel::getScopeKey)
                        .collect(Collectors.toList());

        HTreeMap<String, ScopeModel> scopes = tested.getScopeByKeyMap();
        scopes.keySet().forEach(k -> System.out.printf("key: %s, value: %s%n", k, scopes.get(k)));
    }

    @Test
    void testGetDefaultScope() throws IOException {}

    @Test
    void testGetResourceActionsByResourceTypeKey() {}

    @Test
    void testGetResourceActionsByResourceTypeKeyAndActionName() {}

    private static List<PolicyItemCsvModel> getTestPolicyData(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(
                        p ->
                                new PolicyItemCsvModel(
                                        "a" + p, "b" + p, "c" + p, "d" + p, "ALLOW", "f" + p,
                                        "g" + p))
                .collect(Collectors.toList());
    }
}
