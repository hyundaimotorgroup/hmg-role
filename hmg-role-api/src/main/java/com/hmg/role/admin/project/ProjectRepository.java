package com.hmg.role.admin.project;

import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.admin.project.projections.ProjectProjection;
import com.hmg.role.rbac.scope.Scope;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByKey(String key);

    Optional<Project> findByKeyAndDeletedFalse(String key);

    List<Project> findByKeyInAndDeletedFalse(List<String> keys);

    Page<Project> findAllByDeletedFalseOrderByUpdatedAtDesc(Pageable pageable);

    boolean existsByKeyAndDeletedFalse(String key);

    @Query(
            """
                                SELECT p FROM Project p
                                         WHERE p.deleted = false
                                           AND (
                                             LOWER(REPLACE(REPLACE(p.name, ' ', ''), '_', '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                             OR LOWER(REPLACE(REPLACE(p.key, ' ', ''), '_', '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                           ) ORDER BY p.updatedAt desc
                    """)
    Page<ProjectProjection> findAllByKeyAndNameAndDeletedFalse(
            String searchTerm, Pageable pageable);

    List<Project> findAllByDeletedFalse();

    @Query("select p.defaultScopeRbac from Project p where p = :project")
    Scope getDefaultScopeRbac(Project project);

    @Query("select p.defaultScopeAbac from Project p where p = :project")
    AbacScope getDefaultScopeAbac(Project project);

    @Query(
            """
                SELECT p
                    FROM Project p
                    WHERE p.deleted IS FALSE
                      AND NOT EXISTS (
                        SELECT 1
                        FROM ProjectConfiguration pc
                        WHERE pc.project = p
                          AND pc.configurationKey = :configurationKey
                      )
            """)
    List<Project> findAllByMissingConfiguration(String configurationKey);

    @Query(
            """
                FROM Project p
                WHERE p.key = :projectKey
                AND p.deleted IS FALSE
            """)
    Project getByKey(String projectKey);
}
