package com.hmg.role.rbac.role;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.role.projections.RoleProjection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNameAndProject(String roleName, Project project);

    boolean existsByKeyAndProjectAndDeletedFalse(String key, Project project);

    List<Role> findByKeyInAndProjectAndDeletedFalse(Collection<String> keys, Project project);

    boolean existsByNameInAndProjectAndDeletedFalse(List<String> names, Project project);

    List<Role> findByKeyIn(List<String> roleKeys);

    List<Role> findByNameInAndProjectAndDeletedFalse(List<String> names, Project project);

    @Query(
            """
        SELECT r
        FROM Role r
        WHERE r.project = :project
          AND r.deleted = false
        ORDER BY
          CASE
            when function('REGEXP_LIKE', r.name, '^[0-9]') = 1 then 0
            when function('REGEXP_LIKE', r.name, '^[A-Za-z]') = 1 then 1
            when function('REGEXP_LIKE', r.name, '^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]') = 1 then 2
            WHEN r.name LIKE '\\_%' ESCAPE '\\' THEN 3
            WHEN r.name LIKE '-%' THEN 4
            ELSE 5
          END,
          r.name ASC
        """)
    Page<Role> findByProjectAndDeletedFalseOrderByNameAsc(Project project, Pageable pageable);

    Optional<Role> findByKeyAndProjectAndDeletedFalse(String key, Project project);

    @Query(
            """
                      SELECT r.key As key,
                      COUNT(u.id) AS countUsersPerRole
                      FROM UserScopeRole usr
                      JOIN usr.user u
                      JOIN usr.role r
                      WHERE r.key IN :roleKey
                      AND r.project = :project
                      AND r.deleted = false
                      AND u.deleted = false
                      AND usr.deleted = false
                      GROUP BY r.key
                    """)
    List<RoleProjection> countUsersPerRole(List<String> roleKey, Project project);

    @Query(
            """
                    SELECT r FROM Role r
                    WHERE r.project = :project
                      AND r.deleted = false
                      AND LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!'
                    """)
    Page<Role> searchByProjectAndKeywordName(Project project, String keyword, Pageable pageable);

    @Query(
            """
                    SELECT r FROM Role r
                    WHERE r.project = :project
                      AND r.deleted = false
                      AND LOWER(r.key) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!'
                    """)
    Page<Role> searchByProjectAndKeywordKey(Project project, String keyword, Pageable pageable);

    List<Role> findRoleByKeyInAndProjectAndDeletedFalse(List<String> keys, Project project);

    List<Role> findRoleByKeyInAndProjectIdAndDeletedFalse(List<String> keys, Long projectId);

    List<Role> findRoleByNameIsNullAndDeletedFalse(Pageable pageable);

    @Query(
            """
                    SELECT count(r) FROM Role r
                    WHERE r.project = :project
                      AND r.deleted = false
            """)
    int countRolesByProjectAndDeletedFalse(Project project);
}
