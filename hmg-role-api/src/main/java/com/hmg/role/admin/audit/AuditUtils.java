package com.hmg.role.admin.audit;

import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_AUTHOR_USER_CLASS_DESIGNATOR;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_ENTITY_ID;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_ENTITY_KEY;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_ENTITY_PATH;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_ENTITY_PROJECT_KEY;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_PERMISSION_STRUCTURE;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_REQUEST_ID;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_REQUEST_METHOD;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_REQUEST_PATH;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_SCOPE_KEY;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_USER_AGENT;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_USER_IP;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_PATH_TO_ENTITY_MAP;
import static com.hmg.role.admin.audit.AuditConstants.ENTITY_TO_AUDIT_PATH_MAP;
import static com.hmg.role.admin.audit.AuditConstants.MAX_VIEWABLE_AUDIT_PERIOD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hmg.role.admin.audit.dto.AuditTrailResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailsRequestDto;
import com.hmg.role.admin.audit.enums.AuthorUserClassDesignator;
import com.hmg.role.admin.audit.exceptions.RequestDateRangeTooLongException;
import com.hmg.role.rbac.policy.Policy;
import com.hmg.role.rbac.resourcetype.ResourceType;
import com.hmg.role.rbac.role.Role;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.user.User;
import com.hmg.role.sdk.common.util.Utils;
import com.hmg.role.util.container.Pair;
import com.hmg.role.util.enums.PermissionStructure;
import com.hmg.role.util.serdeutils.SnapshotWriters;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.javers.core.ChangesByCommit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuditUtils { // all properties supposed to be package-private

    private static final ObjectWriter SNAPSHOT_WRITER = SnapshotWriters.shallowObjectWriter();
    private static final String[] USER_IP_HEADERS = {
        "X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP"
    };

    static boolean isEntityMetadataRecorded(Object entityObject) {
        return switch (entityObject) {
            case Policy p -> true;
            case ResourceType r -> true;
            case Role r -> true;
            case User u -> true;
            case Scope s -> true;
            default -> false;
        };
    }

    static String getEntityId(Object entityObject) {
        var id =
                switch (entityObject) {
                    case Policy p -> p.getId();
                    case ResourceType ry -> ry.getId();
                    case Role o -> o.getId();
                    case User u -> u.getId();
                    case Scope s -> s.getScopeId();
                    default -> 0;
                };

        return String.valueOf(id);
    }

    static String getEntityKey(Object entityObject) {
        // the proper manner would be to create something like
        // `abstract class EntityId { public String getKey() }` instead
        // and extend it to all entities
        // but for now this will do
        return switch (entityObject) {
            case Policy p -> p.getKey();
            case ResourceType ry -> ry.getKey();
            case Role o -> o.getKey();
            case User u -> u.getUserKey();
            case Scope s -> s.getKey();
            default -> null;
        };
    }

    static String getEntityProjectName(Object entityObject) {
        return switch (entityObject) {
            case Policy p -> p.getProject().getName();
            case ResourceType ry -> ry.getProject().getName();
            case Role o -> o.getProject().getName();
            case User u -> u.getProject().getName();
            case Scope s -> s.getProject().getName();
            default -> null;
        };
    }

    static String getEntityProjectKey(Object entityObject) {
        return switch (entityObject) {
            case Policy p -> p.getProject().getKey();
            case ResourceType ry -> ry.getProject().getKey();
            case Role o -> o.getProject().getKey();
            case User u -> u.getProject().getKey();
            case Scope s -> s.getProject().getKey();
            default -> null;
        };
    }

    static void validateStartDate(ZonedDateTime startDate, ZonedDateTime today) {
        if (startDate.isBefore(today.minus(MAX_VIEWABLE_AUDIT_PERIOD).minusDays(1))) {
            throw new RequestDateRangeTooLongException();
        }
    }

    static Class<?> findClass(String keyword) {
        return AUDIT_PATH_TO_ENTITY_MAP.get(keyword);
    }

    static Page<AuditTrailResponseDto> convertChangesToPaginatedDto(
            final List<ChangesByCommit> changesList, AuditTrailsRequestDto filters) {
        var mapped =
                // manual slicing due to Javers not suppporting paging
                changesList.stream()
                        .skip((long) filters.getPage() * filters.getSize())
                        .map(AuditUtils::convertChangeToDto)
                        .limit(filters.getSize())
                        .toList();
        var pageable = PageRequest.of(filters.getPage(), filters.getSize());
        return new PageImpl<>(mapped, pageable, changesList.size());
    }

    static AuditTrailResponseDto convertChangeToDto(ChangesByCommit cg) {
        var c = cg.getCommit();
        var props = c.getProperties();
        return AuditTrailResponseDto.builder()
                .commitId(String.valueOf(c.getId()))
                .key(props.get(AUDIT_ATTR_ENTITY_KEY))
                .author(c.getAuthor())
                .ip(props.get(AUDIT_ATTR_USER_IP))
                .entityPath(props.get(AUDIT_ATTR_ENTITY_PATH))
                .requestMethod(props.get(AUDIT_ATTR_REQUEST_METHOD))
                .requestPath(props.get(AUDIT_ATTR_REQUEST_PATH))
                .eventTimestamp(Utils.formatToIso8601String(c.getCommitDateInstant()))
                .requestId(props.get(AUDIT_ATTR_REQUEST_ID))
                .userClassDesignator(props.get(AUDIT_ATTR_AUTHOR_USER_CLASS_DESIGNATOR))
                .build();
    }

    static Optional<String> getMethod(RequestAuditContext ctx, Object entityObject) {
        boolean isDeleted =
                switch (entityObject) {
                    case Policy p -> p.isDeleted();
                    case ResourceType ry -> ry.isDeleted();
                    case Role o -> o.isDeleted();
                    case User u -> u.isDeleted();
                    case Scope s -> s.isDeleted();
                    default -> false;
                };

        if (!isDeleted) {
            return Optional.ofNullable(ctx.getHttpRequestMethod());
        } else {
            return Optional.of("DELETE");
        }
    }

    static String getEntityPath(Object entityObject) {
        // to mitigate Hibernate proxy classes like Policy$HibernateProxy$u0wD4LKP
        String entitySimpleName = entityObject.getClass().getSimpleName().split("\\$")[0];
        return ENTITY_TO_AUDIT_PATH_MAP.get(entitySimpleName);
    }

    static String shallowSerToStr(Object o) throws JsonProcessingException {
        return SNAPSHOT_WRITER.writeValueAsString(o);
    }

    static PermissionStructure getPermissionStructure(String requestPath, Object entityObject) {
        // TODO move to the enum itself to improve encapsulation
        if (requestPath.contains("/rbac")) {
            return PermissionStructure.RBAC;
        } else if (requestPath.contains("/abac")) {
            return PermissionStructure.ABAC;
        } else {
            return null;
        }
    }

    static Map<String, String> wrapMetadata(
            RequestAuditContext ctx,
            Pair<AuthorUserClassDesignator, String> author,
            String entityScopeKey,
            PermissionStructure permissionStructure,
            String entityId,
            String entityKey,
            String entityPath,
            String entityProjectKey,
            String clientIp,
            String userAgent,
            String httpRequestMethod,
            String httpPath) {
        return Map.ofEntries(
                Map.entry(
                        AUDIT_ATTR_PERMISSION_STRUCTURE,
                        String.valueOf(permissionStructure).toLowerCase()),
                Map.entry(AUDIT_ATTR_ENTITY_ID, entityId),
                Map.entry(AUDIT_ATTR_ENTITY_PATH, entityPath),
                Map.entry(AUDIT_ATTR_ENTITY_KEY, entityKey),
                Map.entry(AUDIT_ATTR_ENTITY_PROJECT_KEY, entityProjectKey),
                Map.entry(AUDIT_ATTR_SCOPE_KEY, entityScopeKey),
                Map.entry(AUDIT_ATTR_USER_IP, clientIp),
                Map.entry(AUDIT_ATTR_USER_AGENT, userAgent),
                Map.entry(AUDIT_ATTR_REQUEST_METHOD, httpRequestMethod),
                Map.entry(AUDIT_ATTR_REQUEST_PATH, httpPath),
                Map.entry(AUDIT_ATTR_REQUEST_ID, ctx.getRequestId()),
                Map.entry(AUDIT_ATTR_AUTHOR_USER_CLASS_DESIGNATOR, author.first().name()));
    }

    public static String resolveClientIp(HttpServletRequest request) {
        // get real user IP since sometimes AWS ALB/CloudFlare/nginx replaces it
        for (String headerKeys : USER_IP_HEADERS) {
            String headerValue = request.getHeader(headerKeys);
            if (StringUtils.hasText(headerValue) && !"unknown".equalsIgnoreCase(headerValue)) {
                // For X-Forwarded-For, take the first IP (original client)
                if ("X-Forwarded-For".equalsIgnoreCase(headerKeys)) {
                    int comma = headerValue.indexOf(',');
                    return comma > 0 ? headerValue.substring(0, comma).trim() : headerValue.trim();
                }
                return headerValue.trim();
            }
        }
        return request.getRemoteAddr(); // fallback
    }
}
