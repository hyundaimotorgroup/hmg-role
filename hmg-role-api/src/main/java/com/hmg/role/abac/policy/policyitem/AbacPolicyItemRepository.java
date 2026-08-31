package com.hmg.role.abac.policy.policyitem;

import com.hmg.role.abac.policy.AbacPolicy;
import com.hmg.role.abac.policy.AbacPolicyItem;
import com.hmg.role.abac.resourceset.ResourceSet;
import com.hmg.role.abac.resourceset.action.ResourceSetAction;
import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.abac.userset.UserSet;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.projections.PolicyItemProjection;
import jakarta.persistence.QueryHint;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface AbacPolicyItemRepository extends JpaRepository<AbacPolicyItem, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query(
            """
            select pi
            from AbacPolicyItem pi
            join fetch pi.resourceSetAction ra
            join fetch pi.resourceSetAction.resourceSet rs
            join fetch pi.policy p
            join fetch pi.userSet us
            where p.project = :project
            and pi.userSet in (:userSets)
            and rs in (:resourceSets)
            and ra.actionName in (:actionNames)
            and p.deleted is false
            and pi.deleted is false
            and ra.deleted is false
            and us.deleted is false
            and pi.scopeKey = :scopeKey
            """)
    List<AbacPolicyItem> findAllByResourceActionsAndUserSetAndProject(
            List<ResourceSet> resourceSets,
            List<String> actionNames,
            List<UserSet> userSets,
            Project project,
            String scopeKey);

    List<AbacPolicyItem> findByPolicyAndDeletedFalse(AbacPolicy policy);

    List<AbacPolicyItem> findByPolicyInAndDeletedFalse(List<AbacPolicy> policy);

    List<AbacPolicyItem> findByUserSetInAndPolicy_DeletedFalse(List<UserSet> userSets);

    List<AbacPolicyItem> findByResourceSetAction_ResourceSetInAndPolicy_DeletedFalse(
            List<ResourceSet> resourceSets);

    @Query(
            // WARNING: POSSIBLE PERFORMANCE ISSUE
            // mid-text LIKE matching. Can't be removed due to business logic requirements
            """
        SELECT pi.policyKey AS policyKey,
             function('group_concat', pi.id) AS policyItemIdsCsv
        FROM AbacPolicyItem pi
        WHERE pi.projectKey = :#{#project.key}
            AND (:scope IS NULL OR :scope = pi.scope)
            AND (:userSetKeyLike IS NULL
                OR lower(pi.userSetKey) LIKE concat("%", lower(:userSetKeyLike) , "%") ESCAPE '!')
            AND (:resourceSetKeyLike IS NULL
                OR lower(pi.resourceSetKey) LIKE concat("%", lower(:resourceSetKeyLike) , "%") ESCAPE '!')
            AND pi.deleted IS FALSE
        GROUP BY pi.policyKey
    """)
    Page<PolicyItemProjection> findIdsByCriteria(
            String userSetKeyLike,
            String resourceSetKeyLike,
            Project project,
            AbacScope scope,
            Pageable pageable);

    @Query(
            """
        SELECT pi
        FROM AbacPolicyItem pi
        JOIN FETCH pi.policy p
        JOIN FETCH pi.resourceSetAction ra
        JOIN FETCH ra.resourceSet rs
        JOIN FETCH pi.userSet us
        JOIN FETCH pi.scope s
        WHERE pi.id IN :policyItemIds
    """)
    List<AbacPolicyItem> findByIdIn(List<Long> policyItemIds);

    @Query(
            """
        SELECT count(*) > 0
        FROM AbacPolicyItem pi
        JOIN pi.policy p
        WHERE pi.scope = :scope
            AND p.project = :project
            AND pi.deleted IS FALSE
            AND pi.policy.deleted IS FALSE
            AND p.project.deleted IS FALSE
    """)
    boolean existsByScopeAndProject(AbacScope scope, Project project);

    List<AbacPolicyItem> findByScopeAndPolicyDeletedFalse(AbacScope scope);

    boolean existsByResourceSetActionInAndDeletedFalseAndPolicyProjectAndPolicyDeletedFalse(
            List<ResourceSetAction> actions, Project project);

    @Query(
            """
        SELECT count(pi) > 0
        FROM AbacPolicyItem pi
        JOIN pi.resourceSetAction ra
        JOIN ra.resourceSet rs
        JOIN pi.policy p
        JOIN pi.userSet us
        WHERE p.project = :project
        AND pi.userSet IN :userSets
        AND rs IN :resourceSets
        AND ra.actionName IN :actionNames
        AND p.deleted IS FALSE
        AND pi.deleted IS FALSE
        AND ra.deleted IS FALSE
        AND us.deleted IS FALSE
        AND pi.scopeKey = :scopeKey
        AND (:excludePolicyId IS NULL OR p.id != :excludePolicyId)
    """)
    boolean existsConflictingPolicyItem(
            List<ResourceSet> resourceSets,
            List<String> actionNames,
            List<UserSet> userSets,
            Project project,
            String scopeKey,
            Long excludePolicyId);
}
