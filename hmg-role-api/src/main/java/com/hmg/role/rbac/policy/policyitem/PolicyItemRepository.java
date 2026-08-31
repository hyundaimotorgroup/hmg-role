package com.hmg.role.rbac.policy.policyitem;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.Policy;
import com.hmg.role.rbac.policy.projections.PolicyItemProjection;
import com.hmg.role.rbac.resourcetype.ResourceType;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.scope.Scope;
import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyItemRepository extends JpaRepository<PolicyItem, Long> {

    void deleteAllByPolicy(Policy policy);

    List<PolicyItem> findByIdIn(List<String> ids);

    void deleteAllByPolicyIn(Collection<Policy> policies);

    @Query(
            """
                            from PolicyItem pi
                            join fetch pi.policy p
                            join fetch pi.resourceAction ra
                            join fetch ra.resourceType rt
                            join fetch pi.scope s
                            join fetch pi.role r
                            where pi.deleted is false
                            and p.key = :policyKey
                    """)
    List<PolicyItem> findWithFetchAllByPolicyKey(String policyKey);

    List<PolicyItem> findAllByPolicyKeyIn(Collection<String> policyKeyList);

    List<PolicyItem> findByResourceActionResourceTypeInAndPolicyDeletedIsFalse(
            List<ResourceType> resourceType);

    List<PolicyItem>
            findByPolicyDeletedIsFalseAndResourceActionResourceTypeAndResourceActionActionNameIn(
                    ResourceType resourceType, Collection<String> actionNames);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query(
            """
                    select pi
                    from PolicyItem pi
                    join fetch pi.policy p
                    join fetch pi.resourceAction ra
                    join fetch ra.resourceType rt
                    where rt.project = :project
                    and pi.scope.key = :scope
                    and pi.role.key in (:roles)
                    and rt.key = :resourceTypeKey
                    and ra.actionName in (:actionNames)
                    and p.deleted is false
                    and pi.deleted is false
                    and ra.deleted is false
                    and rt.deleted is false
                    """)
    List<PolicyItem> findAllByResourceActionsAndRolesAndProject(
            String resourceTypeKey,
            List<String> actionNames,
            List<String> roles,
            String scope,
            Project project);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query(
            """
                    from PolicyItem pi
                    join fetch pi.policy p
                    join fetch pi.resourceAction ra
                    join fetch ra.resourceType rt
                    join fetch pi.scope
                    join fetch pi.role
                    where pi.deleted is false
                        and p.deleted is false
                    and p.project = :project
                    and rt.key in (:resourceTypeKeys) and rt.deleted is false
                    and ra.actionName in (:actionNames) and ra.deleted is false
                    and pi.role.key in (:roles) and pi.role.deleted is false
                    and pi.scope.key IN :scopes and pi.scope.deleted is false
                    """)
    List<PolicyItem> findFetchAllFilterByResourceTypeKeysAndActionsAndRolesAndScopes(
            Collection<String> resourceTypeKeys,
            Collection<String> actionNames,
            Collection<String> roles,
            Collection<String> scopes,
            Project project);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query(
            """
                    select pi
                    from PolicyItem pi
                    join fetch pi.policy p
                    join fetch pi.resourceAction ra
                    join fetch ra.resourceType rt
                    where rt.project = :project
                    and p.key != :policyKeyNot
                    and pi.scope.key = :scope
                    and pi.role.key in (:roles)
                    and rt.key = :resourceTypeKey
                    and ra.actionName in (:actionNames)
                    and p.deleted is false
                    and pi.deleted is false
                    and ra.deleted is false
                    and rt.deleted is false
                    """)
    List<PolicyItem> findAllByPolicyKeyNotAndResourceActionsAndRolesAndProject(
            String policyKeyNot,
            String resourceTypeKey,
            List<String> actionNames,
            List<String> roles,
            String scope,
            Project project);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query(
            """
                    select pi
                    from PolicyItem pi
                    join fetch pi.policy p
                    join pi.resourceAction ra
                    join ra.resourceType rt
                    where rt.project = :project
                    and pi.scope.key IN (:scope)
                    and pi.role.key in (:roles)
                    and rt.key IN (:resourceTypeKey)
                    and ra.actionName in (:actionNames)
                    and pi.deleted is false
                    and ra.deleted is false
                    and rt.deleted is false
                    and pi.scope.deleted is false
                    and pi.role.deleted is false
                    and p.deleted is false
                    """)
    List<PolicyItem> findAllByResourceTypeKeyInAndActionNameInAndRoleKeyInAndScopeKeyIn(
            Collection<String> resourceTypeKey,
            Collection<String> actionNames,
            Collection<String> roles,
            Collection<String> scope,
            Project project);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query(
            """
                    select pi
                    from PolicyItem pi
                    join fetch pi.policy p
                    join fetch pi.resourceAction ra
                    join fetch pi.role rl
                    join fetch pi.scope sc
                    join fetch ra.resourceType rt
                    where rt.project = :project
                    and sc.key in (:policyScopes)
                    and rl.key in (:roles)
                    and rt.key in (:resourceTypeKeys)
                    and ra.actionName in (:actionNames)
                    and p.deleted is false
                    and pi.deleted is false
                    and ra.deleted is false
                    and rt.deleted is false
                    """)
    List<PolicyItem> findAllByResourceActionsAndScopesAndRolesAndProject(
            Collection<String> resourceTypeKeys,
            Collection<String> actionNames,
            Collection<String> policyScopes,
            Collection<String> roles,
            Project project);

    @Query(
            """
                            from PolicyItem pi
                            join fetch pi.policy p
                            join fetch pi.resourceAction ra
                            join fetch pi.role rl
                            join fetch pi.scope sc
                            join fetch ra.resourceType rt
                            where p.project = :project
                            and p.key in (:policyKeys)
                            and p.deleted is false
                            and pi.deleted is false
                    """)
    List<PolicyItem> findFetchAllByPolicyKeys(Collection<String> policyKeys, Project project);

    List<PolicyItem> findByRoleInAndPolicyDeletedFalse(List<Role> roles);

    List<PolicyItem> findByScopeAndPolicyDeletedFalse(Scope scope);

    @Query(
            value =
                    """
            SELECT
             pi.policyKey,
             function('group_concat', pi.id) AS policyItemIdsCsv
            FROM PolicyItem pi
            WHERE pi.deleted = false
              AND pi.projectKey = :projectKey
              AND ( :roleKey IS NULL OR pi.roleKey = :roleKey )
              AND ( :scopeKey IS NULL OR pi.scopeKey = :scopeKey )
              AND ( :resourceTypeKey IS NULL OR pi.resourceTypeKey = :resourceTypeKey )
              AND ( :actionName IS NULL OR LOWER(pi.actionName) LIKE LOWER(CONCAT('%', :actionName, '%')) ESCAPE '!' )
            GROUP BY
             pi.policyKey
            """)
    Page<PolicyItemProjection> findHeadersByOptionalParams(
            String projectKey,
            String roleKey,
            String scopeKey,
            String resourceTypeKey,
            String actionName,
            Pageable pageable);
}
