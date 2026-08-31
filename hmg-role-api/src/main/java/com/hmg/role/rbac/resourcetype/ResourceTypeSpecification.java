package com.hmg.role.rbac.resourcetype;

import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.resourceaction.ResourceAction;
import com.hmg.role.rbac.resourcetag.ResourceTag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;

public class ResourceTypeSpecification {

    private ResourceTypeSpecification() {}

    public static Specification<ResourceType> projectIs(Project project) {
        return (root, query, cb) -> cb.equal(root.get("project"), project);
    }

    public static Specification<ResourceType> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<ResourceType> keyIn(Collection<String> keys) {
        return (root, query, cb) -> root.get("key").in(keys);
    }

    public static Specification<ResourceType> parentIn(Collection<ResourceType> parents) {
        return (root, query, cb) -> root.get("parent").in(parents);
    }

    public static Specification<ResourceType> isRoot() {
        return (root, query, cb) -> cb.isNull(root.get("parent"));
    }

    public static Specification<ResourceType> nameLike(String kw) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + kw + "%", '!');
    }

    public static Specification<ResourceType> keyLike(String kw) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("key")), "%" + kw + "%", '!');
    }

    public static Specification<ResourceType> hasTagLike(String kw) {
        return (root, query, cb) -> {
            Join<ResourceType, ResourceTag> tags = root.join("resourceTags", JoinType.LEFT);
            return cb.and(
                    cb.isFalse(tags.get("deleted")),
                    cb.like(cb.lower(tags.get("tag")), "%" + kw + "%", '!'));
        };
    }

    public static Specification<ResourceType> childNameLike(String kw) {
        return (root, query, cb) -> {
            Join<ResourceType, ResourceType> children = root.join("children", JoinType.LEFT);
            return cb.like(cb.lower(children.get("name")), "%" + kw + "%", '!');
        };
    }

    public static Specification<ResourceType> actionNameLike(String kw) {
        return (root, query, cb) -> {
            Join<ResourceType, ResourceAction> actions =
                    root.join("resourceActions", JoinType.INNER);

            // cb.like with non-backslash escape chars produces ESCAPE '' in Hibernate 6 for joined
            // paths — use REGEXP_LIKE to avoid wildcard issues with user-supplied input.
            var matchesAction =
                    cb.greaterThan(
                            cb.function(
                                    "regexp_like",
                                    Integer.class,
                                    cb.lower(actions.get("actionName")),
                                    cb.literal(escapeForMysqlRegex(kw.toLowerCase()))),
                            0);
            var actionIsNotDeleted = cb.isFalse(actions.get("deleted"));

            return cb.and(matchesAction, actionIsNotDeleted);
        };
    }

    private static String escapeForMysqlRegex(String input) {
        return input.replaceAll("([.*+?\\[\\](){}^$|\\\\])", "\\\\$1");
    }

    public static Specification<ResourceType> childKeyLike(String kw) {
        return (root, query, cb) -> {
            Join<ResourceType, ResourceType> children = root.join("children", JoinType.LEFT);
            return cb.like(cb.lower(children.get("key")), "%" + kw + "%", '!');
        };
    }

    public static Specification<ResourceType> childTagLike(String kw) {
        return (root, query, cb) -> {
            Join<ResourceType, ResourceType> children = root.join("children", JoinType.LEFT);
            Join<ResourceType, ResourceTag> childTags =
                    children.join("resourceTags", JoinType.LEFT);
            return cb.and(
                    cb.isFalse(childTags.get("deleted")),
                    cb.like(cb.lower(childTags.get("tag")), "%" + kw + "%", '!'));
        };
    }
}
