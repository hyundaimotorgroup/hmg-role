package com.hmg.role.rbac.userscoperole;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.user.User;
import com.hmg.role.rbac.userscoperole.projections.ScopeUserProjection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserScopeRoleRepository extends JpaRepository<UserScopeRole, Long> {
    @Query(
"""
    SELECT sr FROM UserScopeRole sr
    JOIN FETCH sr.role r
    JOIN FETCH sr.scope s
    WHERE sr.user = :user
    AND sr.deleted = false
    AND s.deleted = false
    AND r.deleted = false
""")
    List<UserScopeRole> findActiveByUserWithRoleAndScope(User user);

    @Query(
"""
    SELECT sr.role
    FROM UserScopeRole sr
    WHERE sr.user.userKey = :userKey
    AND sr.scope.key = :scopeKey
    AND sr.scope.project = :project
    AND sr.deleted = false
""")
    Stream<Role> findRolesByUserAndScope(String userKey, String scopeKey, Project project);

    @Query(
"""
    SELECT sr FROM UserScopeRole sr
    JOIN FETCH sr.role r
    JOIN FETCH sr.scope s
    WHERE sr.user IN :users
    AND sr.deleted = false
    AND s.deleted = false
    AND r.deleted = false
""")
    List<UserScopeRole> findActiveByUser(List<User> users);

    List<UserScopeRole> findByRoleInAndDeletedFalseAndUserDeletedFalse(List<Role> roles);

    List<UserScopeRole> findByScopeAndDeletedFalseAndUserDeletedFalse(Scope scope);

    @Query(
            // TODO: REFACTOR - misrequirement; fix ordering
            value =
                    """
                    SELECT
                        usr.user.userKey AS userKey,
                        usr.user.name    AS userName,
                        usr.scope.key    AS scopeKey,
                        usr.scope.name   AS scopeName
                    FROM UserScopeRole usr
                    WHERE usr.role.key = :roleKey
                    AND usr.role.project = :project
                    AND usr.role.deleted = false
                    AND usr.user.deleted = false
                    AND usr.scope.deleted = false
                    AND usr.deleted = false
                    AND (:scopeKey IS NULL OR usr.scope.key = :scopeKey)
                    AND (:userNameOrUserKeyILike IS NULL OR (
                        LOWER(usr.user.name) LIKE LOWER(CONCAT('%', :userNameOrUserKeyILike, '%')) ESCAPE '\\'
                        OR LOWER(usr.user.userKey) LIKE LOWER(CONCAT('%', :userNameOrUserKeyILike, '%')) ESCAPE '\\'
                    ))
                    ORDER BY
                        CASE
                            WHEN function('REGEXP_LIKE', usr.scope.name, '^[0-9]') = 1 THEN 0
                            WHEN function('REGEXP_LIKE', usr.scope.name, '^[A-Za-z]') = 1 THEN 1
                            WHEN function('REGEXP_LIKE', usr.scope.name, '^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]') = 1 THEN 2
                            WHEN function('REGEXP_LIKE', usr.scope.name, '^_') = 1 THEN 3
                            WHEN function('REGEXP_LIKE', usr.scope.name, '^-') = 1 THEN 4
                            ELSE 9
                        END ASC,
                        usr.scope.name ASC,
                        CASE
                            WHEN function('REGEXP_LIKE', usr.user.name, '^[0-9]') = 1 THEN 0
                            WHEN function('REGEXP_LIKE', usr.user.name, '^[A-Za-z]') = 1 THEN 1
                            WHEN function('REGEXP_LIKE', usr.user.name, '^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]') = 1 THEN 2
                            WHEN function('REGEXP_LIKE', usr.user.name, '^_') = 1 THEN 3
                            WHEN function('REGEXP_LIKE', usr.user.name, '^-') = 1 THEN 4
                            ELSE 9
                        END ASC,
                        usr.user.name,
                        CASE
                            WHEN function('REGEXP_LIKE', usr.user.userKey, '^[0-9]') = 1 THEN 0
                            WHEN function('REGEXP_LIKE', usr.user.userKey, '^[A-Za-z]') = 1 THEN 1
                            WHEN function('REGEXP_LIKE', usr.user.userKey, '^[\\x{3130}-\\x{318F}\\x{AC00}-\\x{D7AF}]') = 1 THEN 2
                            WHEN function('REGEXP_LIKE', usr.user.userKey, '^_') = 1 THEN 3
                            WHEN function('REGEXP_LIKE', usr.user.userKey, '^-') = 1 THEN 4
                            ELSE 9
                        END,
                        usr.user.userKey
                    """)
    Page<ScopeUserProjection> findScopeUsersByRoleKey(
            String roleKey,
            Project project,
            String scopeKey,
            String userNameOrUserKeyILike,
            Pageable pageable);
}
