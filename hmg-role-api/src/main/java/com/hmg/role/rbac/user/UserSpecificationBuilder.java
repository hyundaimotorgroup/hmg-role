package com.hmg.role.rbac.user;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.userscoperole.UserScopeRole;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecificationBuilder {

    private Specification<User> userSpec = Specification.where(isNotDeleted());

    public Specification<User> build() {
        return userSpec;
    }

    private UserSpecificationBuilder and(Specification<User> other) {
        userSpec = userSpec.and(other);
        return this;
    }

    private UserSpecificationBuilder or(Specification<User> other) {
        userSpec = userSpec.or(other);
        return this;
    }

    public UserSpecificationBuilder andProjectEqual(Project project) {
        return and((root, query, cb) -> cb.equal(root.get(User.PROP_PROJECT), project));
    }

    private Specification<User> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get(UserScopeRole.PROP_DELETED));
    }

    public UserSpecificationBuilder fetchScopeRoles() {
        return and(
                (root, query, cb) -> {
                    // Always distinct so COUNT(DISTINCT id) is used in count queries,
                    // preventing row inflation from independent user_scope_roles JOINs.
                    query.distinct(true);
                    if (!Long.class.equals(query.getResultType())) {
                        Fetch<User, UserScopeRole> scopeRolesFetch =
                                root.fetch(User.PROP_SCOPEDROLES, JoinType.INNER);
                        scopeRolesFetch.fetch(UserScopeRole.PROP_SCOPE, JoinType.INNER);
                        scopeRolesFetch.fetch(UserScopeRole.PROP_ROLE, JoinType.INNER);
                    }
                    return null; // No predicate, just fetch
                });
    }

    public UserSpecificationBuilder fetchScopeRolesOrNoScopeRoles() {
        return and(
                (root, query, cb) -> {
                    query.distinct(true);
                    if (!Long.class.equals(query.getResultType())) {
                        Fetch<User, UserScopeRole> fetch =
                                root.fetch(User.PROP_SCOPEDROLES, JoinType.LEFT);
                        @SuppressWarnings("unchecked")
                        Join<User, UserScopeRole> join = (Join<User, UserScopeRole>) fetch;
                        // only join rows where deleted = false
                        join.on(cb.isFalse(join.get(UserScopeRole.PROP_DELETED)));

                        join.fetch(UserScopeRole.PROP_SCOPE, JoinType.LEFT);
                        join.fetch(UserScopeRole.PROP_ROLE, JoinType.LEFT);
                    }
                    return cb.conjunction();
                });
    }

    public UserSpecificationBuilder andUserNameILike(String userName) {
        return and(userNameILike(userName));
    }

    private Specification<User> userNameILike(String userName) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get(User.PROP_NAME)),
                        "%" + escapeLike(userName.toLowerCase()) + "%",
                        '!');
    }

    public UserSpecificationBuilder andUserKeyOrUserNameILike(String userKeyOrUserName) {
        Specification<User> userKeyOrUserNameILike =
                (root, query, cb) -> {
                    var escaped = escapeLike(userKeyOrUserName.toLowerCase());
                    var nameLike =
                            cb.like(cb.lower(root.get(User.PROP_NAME)), "%" + escaped + "%", '!');
                    var keyLike =
                            cb.like(
                                    cb.lower(root.get(User.PROP_USERKEY)),
                                    "%" + escaped + "%",
                                    '!');
                    return cb.or(nameLike, keyLike);
                };
        return and(userKeyOrUserNameILike);
    }

    public UserSpecificationBuilder andUserKeyILike(String userKey) {
        return and(userKeyILike(userKey));
    }

    private Specification<User> userKeyILike(String userKey) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get(User.PROP_USERKEY)),
                        "%" + escapeLike(userKey.toLowerCase()) + "%",
                        '!');
    }

    public UserSpecificationBuilder andScopeKeyEqual(String scopeKey) {
        return and(
                (root, query, cb) -> {
                    Join<User, UserScopeRole> scopeRolesJoin =
                            root.join(User.PROP_SCOPEDROLES, JoinType.INNER);

                    var scopeRoleIsNotDeleted =
                            cb.isFalse(scopeRolesJoin.get(UserScopeRole.PROP_DELETED));

                    Join<UserScopeRole, Scope> scopeJoin =
                            scopeRolesJoin.join(UserScopeRole.PROP_SCOPE, JoinType.INNER);
                    var scopeKeyEqual = cb.equal(scopeJoin.get(Scope.PROP_SCOPE_KEY), scopeKey);
                    var scopeIsNotDeleted = cb.isFalse(scopeJoin.get(Scope.PROP_DELETED));
                    return cb.and(scopeRoleIsNotDeleted, scopeKeyEqual, scopeIsNotDeleted);
                });
    }

    public UserSpecificationBuilder andRoleKeyEqual(String roleKey) {
        return and(
                (root, query, cb) -> {
                    Join<User, UserScopeRole> scopeRolesJoin =
                            root.join(User.PROP_SCOPEDROLES, JoinType.INNER);

                    var scopeRoleIsNotDeleted =
                            cb.isFalse(scopeRolesJoin.get(UserScopeRole.PROP_DELETED));

                    Join<UserScopeRole, Role> roleJoin =
                            scopeRolesJoin.join(UserScopeRole.PROP_ROLE, JoinType.INNER);
                    var roleKeyEqual = cb.equal(roleJoin.get(Role.PROP_KEY), roleKey);
                    var roleIsNotDeleted = cb.isFalse(roleJoin.get(Role.PROP_DELETED));

                    return cb.and(scopeRoleIsNotDeleted, roleKeyEqual, roleIsNotDeleted);
                });
    }

    public UserSpecificationBuilder andRoleNameILike(String keyword) {
        return and(
                (root, query, cb) -> {
                    Subquery<Long> sq = query.subquery(Long.class);
                    Root<UserScopeRole> usrRoot = sq.from(UserScopeRole.class);
                    Join<UserScopeRole, Role> rJoin =
                            usrRoot.join(UserScopeRole.PROP_ROLE, JoinType.INNER);
                    sq.select(cb.literal(1L))
                            .where(
                                    cb.equal(usrRoot.get(UserScopeRole.PROP_USER), root),
                                    cb.isFalse(usrRoot.get(UserScopeRole.PROP_DELETED)),
                                    cb.isFalse(rJoin.get(Role.PROP_DELETED)),
                                    cb.like(
                                            cb.lower(rJoin.get("name")),
                                            "%" + escapeLike(keyword.toLowerCase()) + "%",
                                            '!'));
                    return cb.exists(sq);
                });
    }

    private static String escapeLike(String input) {
        if (input == null) return null;
        return input.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
