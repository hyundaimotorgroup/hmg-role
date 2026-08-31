package com.hmg.role.abac.scope;

import com.hmg.role.admin.project.Project;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AbacScopeRepository extends JpaRepository<AbacScope, Long> {
    Optional<AbacScope> findById(long id);

    Page<AbacScope> findByIdIn(List<Long> ids, Pageable pageable);

    @Query(
            "FROM AbacScope s WHERE (s.key = :key OR s.name = :name) AND s.project = :project AND s.deleted = false")
    Optional<AbacScope> findByKeyOrNameAndProjectAndDeletedFalse(
            String key, String name, Project project);

    Optional<AbacScope> findByNameAndProjectAndDeletedFalse(String name, Project project);

    @Query(
            """
            SELECT s FROM AbacScope s
            WHERE s.project = :project AND s.deleted = false
            ORDER BY
              CASE
                WHEN function('REGEXP_LIKE', s.name, '^[0-9]') = 1 THEN 0
                WHEN function('REGEXP_LIKE', s.name, '^[A-Za-z]') = 1 THEN 1
                WHEN function('REGEXP_LIKE', s.name, '^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]') = 1 THEN 2
                WHEN s.name LIKE '\\_%' ESCAPE '\\' THEN 3
                WHEN s.name LIKE '-%' THEN 4
                ELSE 5
              END,
              s.name ASC
            """)
    Page<AbacScope> findByProjectAndDeletedIsFalseOrderByNameAsc(
            Project project, Pageable pageable);

    Optional<AbacScope> findByKeyAndProjectAndDeletedFalse(String scopeKey, Project project);

    Optional<AbacScope> findByKeyAndProjectAndDeletedIsFalse(String scopeKey, Project projectData);

    boolean existsByKeyInAndProjectAndDeletedFalse(List<String> scopeKeys, Project project);

    List<AbacScope> findByKeyInAndProjectAndDeletedFalse(
            Collection<String> resourcesScope, Project project);
}
