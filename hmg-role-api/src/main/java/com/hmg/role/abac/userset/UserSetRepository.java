package com.hmg.role.abac.userset;

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
public interface UserSetRepository extends JpaRepository<UserSet, Long> {

    @Query(
            // WARNING: POSSIBLE PERFORMANCE ISSUE
            // mid-text LIKE matching. Can't be removed due to business logic requirements
            // TODO: REFACTOR - misrequirement; fix ordering
            """
        SELECT u
        FROM UserSet u
        WHERE u.project = :project
            AND u.deleted IS FALSE
            AND (
                (:keyLike IS NULL OR lower(u.key) LIKE concat("%", lower(:keyLike) , "%") ESCAPE '!')
                AND (:nameLike IS NULL OR lower(u.name) LIKE concat("%", lower(:nameLike) , "%") ESCAPE '!')
            )
        ORDER BY
          CASE
            WHEN function('REGEXP_LIKE', u.name, '^[0-9]') = 1 then 0
            WHEN function('REGEXP_LIKE', u.name, '^[A-Za-z]') = 1 then 1
            WHEN function('REGEXP_LIKE', u.name, '^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]') = 1 then 2
            WHEN u.name LIKE '\\_%' ESCAPE '\\' THEN 3
            WHEN u.name LIKE '-%' THEN 4
            ELSE 5
          END,
          u.name ASC
        """) // user asked for "extraordinary" ordering, thus this
    Page<UserSet> findBySearchParameterAndDeletedFalse(
            // TODO convert to use Specification<> later if necessary
            String keyLike, String nameLike, Project project, Pageable pageable);

    Optional<UserSet> findByKeyAndProjectAndDeletedFalse(String key, Project project);

    List<UserSet> findByKeyInAndProjectAndDeletedFalse(List<String> keys, Project project);

    @Query(
            """
            SELECT DISTINCT userset
            FROM UserSet userset
            JOIN fetch userset.parents userSetParent
            WHERE userSetParent IN (:parents)
            AND userset.project = :project
            AND userset.deleted = false
            AND userSetParent.project = :project
            AND userSetParent.deleted = false
        """)
    List<UserSet> findByParentsInAndProjectAndDeletedFalse(List<UserSet> parents, Project project);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query(
            """
                select us
                from UserSetOperand uco
                join uco.conditionOperand co
                join uco.userSetCondition uc
                join uc.userSet us
                WHERE co.operand IN (:attributes)
                AND us.project = :project
                AND us.deleted is false
            """)
    List<UserSet> findAllByAttributesAndProject(Collection<String> attributes, Project project);

    int countByProjectAndDeletedFalse(Project project);
}
