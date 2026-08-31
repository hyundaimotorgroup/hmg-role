package com.hmg.role.abac.resourceset;

import com.hmg.role.admin.project.Project;
import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceSetRepository extends JpaRepository<ResourceSet, Long> {

    @Query(
            // WARNING: POSSIBLE PERFORMANCE ISSUE
            // mid-text LIKE matching. Can't be removed due to business logic requirements
            // TODO: REFACTOR - misrequirement; fix ordering
            """
        SELECT DISTINCT r
        FROM ResourceSet r
        LEFT JOIN r.actions a
        WHERE r.project = :project
            AND r.deleted IS FALSE
            AND (
                (:keyLike IS NULL OR lower(r.key) LIKE concat("%", lower(:keyLike) , "%") ESCAPE '!')
                AND (:nameLike IS NULL OR lower(r.name) LIKE concat("%", lower(:nameLike) , "%") ESCAPE '!')
                AND (:actionLike IS NULL OR lower(a.actionName) LIKE concat("%", lower(:actionLike) , "%") ESCAPE '!')
            )
        ORDER BY
          CASE
            WHEN function('REGEXP_LIKE', r.name, '^[0-9]') = 1 then 0
            WHEN function('REGEXP_LIKE', r.name, '^[A-Za-z]') = 1 then 1
            WHEN function('REGEXP_LIKE', r.name, '^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]') = 1 then 2
            WHEN r.name LIKE '\\_%' ESCAPE '\\' THEN 3
            WHEN r.name LIKE '-%' THEN 4
            ELSE 5
          END,
          r.name ASC
        """)
    Page<ResourceSet> findBySearchParametersAndDeletedFalse(
            String keyLike, String nameLike, String actionLike, Project project, Pageable pageable);

    Optional<ResourceSet> findByKeyAndProjectAndDeletedFalse(String key, Project project);

    List<ResourceSet> findByKeyInAndProjectAndDeletedFalse(
            Collection<String> keys, Project project);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query(
            """
               SELECT rs
               FROM ResourceSetOperand rso
               join rso.resourceSetCondition rss
               join rss.resourceSet rs
               join rso.conditionOperand rsco
               WHERE rsco.operand IN (:attributes)
               AND rs.project = :project
               AND rs.deleted is false
            """)
    List<ResourceSet> findByAttributesAndProject(Collection<String> attributes, Project project);

    List<ResourceSet> findByParentInAndProjectAndDeletedFalse(
            List<ResourceSet> parents, Project project);

    int countByProjectAndDeletedFalse(Project project);
}
