package com.hmg.role.rbac.user;

import com.hmg.role.admin.project.Project;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByUserKeyAndProjectAndDeletedFalse(String key, Project project);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Optional<User> findByUserKeyAndProjectAndDeletedFalse(String userKey, Project project);

    @Query(
            """
    from User u
    join fetch u.scopedRoles sr
    join fetch sr.scope s
    join fetch sr.role r
    where u.project = :project
    and u.userKey = :userKey
    and u.deleted = false
    and sr.deleted = false
    and s.deleted = false
    and r.deleted = false
    """)
    Optional<User> findWithScopeRolesByUserKey(String userKey, Project project);

    List<User> findByUserKeyInAndProjectAndDeletedFalse(List<String> userKeys, Project project);

    @Query(
"""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.roles
    LEFT JOIN FETCH u.project
    WHERE u.deleted = false
""")
    List<User> findUsersForMigration();
}
