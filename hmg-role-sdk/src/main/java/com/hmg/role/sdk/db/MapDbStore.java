package com.hmg.role.sdk.db;

import com.hmg.role.sdk.db.keys.PolicyItemMapDbKey;
import com.hmg.role.sdk.db.keys.ResourceActionMapDbKey;
import com.hmg.role.sdk.db.keys.RoleByUserScopeMapDbKey;
import com.hmg.role.sdk.rbac.permission.model.ResourceActionModel;
import com.hmg.role.sdk.rbac.permission.model.RoleModel;
import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import com.hmg.role.sdk.reader.model.PolicyItemCsvModel;
import com.hmg.role.sdk.storemanager.models.ScopeMetadataEntry;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

@Slf4j
public class MapDbStore {

    private final DB db;

    // TODO add checking to verify it isn't empty during reading
    @Getter private final BTreeMap<PolicyItemMapDbKey, PolicyItemCsvModel> policyItemByKeyMap;
    @Getter private final HTreeMap<String, RoleModel> rolesByKeyMap;
    @Getter private final BTreeMap<RoleByUserScopeMapDbKey, RoleModel> rolesByUserScopeKeyMap;
    @Getter private final HTreeMap<String, ScopeModel> scopeByKeyMap;
    @Getter private final BTreeMap<ResourceActionMapDbKey, ResourceActionModel> resourceActionMap;

    @Builder
    private MapDbStore(DBMaker.Maker dbMaker) {
        this.db = dbMaker.make();
        policyItemByKeyMap =
                db.treeMap("policy_item")
                        .keySerializer(Serializer.JAVA)
                        .valueSerializer(Serializer.JAVA)
                        .createOrOpen();
        rolesByKeyMap =
                db.hashMap("roles_by_key")
                        .keySerializer(Serializer.STRING)
                        .valueSerializer(Serializer.JAVA)
                        .createOrOpen();
        rolesByUserScopeKeyMap =
                db.treeMap("roles_by_user_scope")
                        .keySerializer(Serializer.JAVA)
                        .valueSerializer(Serializer.JAVA)
                        .createOrOpen();
        scopeByKeyMap =
                db.hashMap("scope_by_key")
                        .keySerializer(Serializer.STRING)
                        .valueSerializer(Serializer.JAVA)
                        .createOrOpen();
        resourceActionMap =
                db.treeMap("resource_action")
                        .keySerializer(Serializer.JAVA)
                        .valueSerializer(Serializer.JAVA)
                        .createOrOpen();
    }

    public void insert(List<PolicyItemCsvModel> policyCsv) {
        for (PolicyItemCsvModel policy : policyCsv) {
            try {
                insertPolicy(policy);
                insertRole(policy);
                insertScope(policy);
                insertResourceType(policy);
                db.commit();
            } catch (Exception e) {
                log.error(
                        "Failed to insert policy item to database, message: {}", e.getMessage(), e);
                db.rollback();
            }
        }
    }

    public void delete(ScopeMetadataEntry scopeMetadatum) {
        try {
            deletePoliciesByScopeKey(scopeMetadatum.getScopeKey());
            refreshRoles(scopeMetadatum.getScopeKey());
            refreshScopes(scopeMetadatum.getScopeKey());
            refreshResourceTypes(scopeMetadatum.getScopeKey());
            db.commit();
        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    public void update(ScopeMetadataEntry scopeMetadatum, List<PolicyItemCsvModel> policies)
            throws IOException {
        delete(scopeMetadatum);
        insert(policies);
    }

    private void insertPolicy(PolicyItemCsvModel policy) {
        PolicyItemMapDbKey id = new PolicyItemMapDbKey(policy);
        policyItemByKeyMap.put(id, policy);
    }

    private void insertRole(PolicyItemCsvModel policy) {
        insertRoleByKey(policy);
        insertRoleByUserAndScope(policy);
    }

    private void insertRoleByKey(PolicyItemCsvModel policy) {
        if (rolesByKeyMap.containsKey(policy.getRoleKey())) {
            rolesByKeyMap.put(policy.getRoleKey(), policy);
        }
    }

    private void insertRoleByUserAndScope(PolicyItemCsvModel policy) {
        if (rolesByUserScopeKeyMap.containsKey(new RoleByUserScopeMapDbKey(policy))) {
            rolesByUserScopeKeyMap.put(new RoleByUserScopeMapDbKey(policy), policy);
        }
    }

    private void insertScope(PolicyItemCsvModel policy) {
        insertScopeByKey(policy);
    }

    private void insertScopeByKey(PolicyItemCsvModel policy) {
        if (!scopeByKeyMap.containsKey(policy.getScopeKey())) {
            scopeByKeyMap.put(policy.getScopeKey(), policy);
        }
    }

    private void insertResourceType(PolicyItemCsvModel policyCsv) {
        if (resourceActionMap.containsKey(new ResourceActionMapDbKey(policyCsv))) {
            resourceActionMap.put(new ResourceActionMapDbKey(policyCsv), policyCsv);
        }
    }

    private void deletePoliciesByScopeKey(String scopeKey) {
        PolicyItemMapDbKey key = new PolicyItemMapDbKey(scopeKey, null, null, null);
        NavigableSet<PolicyItemMapDbKey> map =
                policyItemByKeyMap.subMap(key, true, key, true).keySet();
        for (PolicyItemMapDbKey policyItemMapDbKey : map) {
            policyItemByKeyMap.remove(policyItemMapDbKey);
        }
    }

    private void refreshRoles(String scopeKey) {
        Map<String, RoleModel> newRolesByKey =
                policyItemByKeyMap.values().stream()
                        .collect(
                                Collectors.toMap(
                                        RoleModel::getRoleKey, Function.identity(), (a, b) -> b));

        rolesByKeyMap.clear();
        rolesByKeyMap.putAll(newRolesByKey);

        Map<RoleByUserScopeMapDbKey, RoleModel> newRolesByUserScopeKey =
                policyItemByKeyMap.values().stream()
                        .collect(
                                Collectors.toMap(
                                        RoleByUserScopeMapDbKey::new, Function.identity()));
        rolesByUserScopeKeyMap.clear();
        rolesByUserScopeKeyMap.putAll(newRolesByUserScopeKey);
    }

    private void refreshScopes(String scopeKey) {
        Map<String, ScopeModel> newScopeKeys =
                policyItemByKeyMap.values().stream()
                        .collect(Collectors.toMap(ScopeModel::getScopeKey, Function.identity()));

        scopeByKeyMap.clear();
        scopeByKeyMap.putAll(newScopeKeys);
    }

    private void refreshResourceTypes(String scopeKey) {
        Map<ResourceActionMapDbKey, ResourceActionModel> newResourceActionModels =
                policyItemByKeyMap.values().stream()
                        .collect(
                                Collectors.toMap(ResourceActionMapDbKey::new, Function.identity()));
        resourceActionMap.clear();
        resourceActionMap.putAll(newResourceActionModels);
    }
}
