package com.hmg.role.rbac.policy;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.policy.projections.PolicyCountProjection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    boolean existsByKeyAndProjectAndDeletedFalse(String key, Project project);

    Optional<Policy> findByKeyAndProjectAndDeletedFalse(String key, Project project);

    List<Policy> findByKeyInAndProjectAndDeletedFalse(Collection<String> key, Project project);

    @Query(
            """
                    SELECT p FROM Policy p
                    WHERE p.project = :project
                    AND p.deleted = false
                    AND p.key in :policyKeys
                    AND NOT EXISTS (
                        SELECT i FROM PolicyItem i
                        WHERE i.policy = p AND i.deleted = false
                    )
                    """)
    List<Policy> findAllByKeyInAndHasNoItems(Collection<String> policyKeys, Project project);

    @Query(
            """
                    SELECT count(DISTINCT policy.id) as policyCount
                    FROM PolicyItem policyItem
                    JOIN policyItem.policy policy
                    JOIN policyItem.resourceAction resourceAction
                    JOIN resourceAction.resourceType resourceType
                    JOIN policyItem.role role
                    JOIN policyItem.scope scope
                    WHERE policy.project = :project
                      AND policy.deleted = false
                      AND policyItem.deleted = false
                      AND (:resourceType IS NULL OR resourceType.key = :resourceType)
                      AND (:roleKey IS NULL OR role.key = :roleKey)
                      AND (:scopeKey IS NULL OR scope.key = :scopeKey)
                      AND (:action IS NULL OR LOWER(resourceAction.actionName) LIKE LOWER(CONCAT('%', :action, '%')) ESCAPE '!')
                    """)
    PolicyCountProjection countByOptionalParams(
            Project project, String resourceType, String roleKey, String scopeKey, String action);
}
