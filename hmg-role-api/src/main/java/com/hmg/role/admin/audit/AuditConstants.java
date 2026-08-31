package com.hmg.role.admin.audit;

import com.hmg.role.rbac.policy.Policy;
import com.hmg.role.rbac.resourcetype.ResourceType;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.user.User;
import com.hmg.role.util.container.Triple;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuditConstants {
    public static final String[] AUDIT_TRAIL_READ_PERMISSIONS =
            new String[] {"read-audit-permission"};

    public static final Set<String> AUDITABLE_PROJECT_NAMES = Set.of("hCloud");

    public static final Period MAX_VIEWABLE_AUDIT_PERIOD = Period.ofDays(180);

    // BEGIN audit attributes
    public static final String AUDIT_ATTR_PERMISSION_STRUCTURE =
            "entity.permissionStructure"; // RBAC/ABAC
    public static final String AUDIT_ATTR_ENTITY_ID = "entity.id";
    public static final String AUDIT_ATTR_ENTITY_KEY = "entity.key";
    public static final String AUDIT_ATTR_ENTITY_PATH = "entity.path";
    public static final String AUDIT_ATTR_SCOPE_KEY = "entity.scopeKey";
    public static final String AUDIT_ATTR_ENTITY_PROJECT_KEY = "entity.projectKey";
    public static final String AUDIT_ATTR_USER_IP = "user.ip";
    public static final String AUDIT_ATTR_USER_AGENT = "user.agent";
    public static final String AUDIT_ATTR_REQUEST_METHOD = "request.method";
    public static final String AUDIT_ATTR_REQUEST_PATH = "request.requestPath";
    public static final String AUDIT_ATTR_REQUEST_ID = "request.id";
    public static final String AUDIT_ATTR_AUTHOR_USER_CLASS_DESIGNATOR = "user.classDesignator";

    public static final Set<String> AUDIT_METADATA_ATTRIBUTES =
            Set.of(
                    AUDIT_ATTR_ENTITY_ID,
                    AUDIT_ATTR_ENTITY_KEY,
                    AUDIT_ATTR_ENTITY_PATH,
                    AUDIT_ATTR_SCOPE_KEY,
                    AUDIT_ATTR_ENTITY_PROJECT_KEY,
                    AUDIT_ATTR_USER_IP,
                    AUDIT_ATTR_USER_AGENT,
                    AUDIT_ATTR_REQUEST_METHOD,
                    AUDIT_ATTR_REQUEST_PATH,
                    AUDIT_ATTR_REQUEST_ID,
                    AUDIT_ATTR_AUTHOR_USER_CLASS_DESIGNATOR);
    // END audit attributes

    public static final String ALL_ENTITY_PATH_IDENTIFIER = "All";
    public static final String RBAC_ENTITY_PATH_IDENTIFIER = "rbac/";

    private static final List<Triple<Class<?>, String, String>> AUDIT_URI_ENTITY_MAPPING =
            List.of(
                    // RBAC only for now, based on the requirements
                    // in the future when ABAC is to be added
                    // this list has to be updated as well
                    new Triple<>(Policy.class, "rbac/policy", ".*\\/policies"),
                    new Triple<>(ResourceType.class, "rbac/resourceType", ".*\\/resource-types"),
                    new Triple<>(Role.class, "rbac/role", ".*\\/roles"),
                    new Triple<>(User.class, "rbac/user", ".*\\/users"),
                    new Triple<>(Scope.class, "rbac/scope", ".*\\/scopes"));

    // URI paths to audit
    public static final List<Pattern> AUDIT_URI_PATH_REGEXES =
            AUDIT_URI_ENTITY_MAPPING.stream()
                    .map(Triple::c)
                    .map(Pattern::compile)
                    .collect(Collectors.toList());

    public static final Map<String, Class<?>> AUDIT_PATH_TO_ENTITY_MAP =
            AUDIT_URI_ENTITY_MAPPING.stream().collect(Collectors.toMap(Triple::b, Triple::a));

    public static final List<String> AUDIT_ENTITY_PATHS =
            AUDIT_PATH_TO_ENTITY_MAP.keySet().stream().toList();

    public static final Map<String, String> ENTITY_TO_AUDIT_PATH_MAP =
            AUDIT_URI_ENTITY_MAPPING.stream()
                    .collect(Collectors.toMap(t -> t.a().getSimpleName(), Triple::b));
}
