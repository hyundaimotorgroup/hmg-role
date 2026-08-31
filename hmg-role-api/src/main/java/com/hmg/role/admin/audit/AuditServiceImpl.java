package com.hmg.role.admin.audit;

import static com.hmg.role.admin.audit.AuditConstants.ALL_ENTITY_PATH_IDENTIFIER;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_ENTITY_KEY;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_ENTITY_PATH;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_ENTITY_PROJECT_KEY;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_SCOPE_KEY;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ATTR_USER_IP;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_ENTITY_PATHS;
import static com.hmg.role.admin.audit.AuditConstants.AUDIT_PATH_TO_ENTITY_MAP;
import static com.hmg.role.admin.audit.AuditConstants.RBAC_ENTITY_PATH_IDENTIFIER;
import static com.hmg.role.admin.audit.AuditUtils.convertChangesToPaginatedDto;
import static com.hmg.role.admin.audit.AuditUtils.findClass;
import static com.hmg.role.admin.audit.AuditUtils.getEntityId;
import static com.hmg.role.admin.audit.AuditUtils.getEntityKey;
import static com.hmg.role.admin.audit.AuditUtils.getEntityPath;
import static com.hmg.role.admin.audit.AuditUtils.getEntityProjectKey;
import static com.hmg.role.admin.audit.AuditUtils.getMethod;
import static com.hmg.role.admin.audit.AuditUtils.getPermissionStructure;
import static com.hmg.role.admin.audit.AuditUtils.isEntityMetadataRecorded;
import static com.hmg.role.admin.audit.AuditUtils.shallowSerToStr;
import static com.hmg.role.admin.audit.AuditUtils.validateStartDate;
import static com.hmg.role.admin.audit.enums.AuditFilterKeywordTypes.valuesAsList;
import static com.hmg.role.admin.audit.enums.AuthorUserClassDesignator.USER_ID;
import static com.hmg.role.util.Constants.NOT_AVAILABLE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hmg.role.admin.audit.dto.AuditFilterResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailDetailsRequestDto;
import com.hmg.role.admin.audit.dto.AuditTrailDetailsResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailResponseDto;
import com.hmg.role.admin.audit.dto.AuditTrailsRequestDto;
import com.hmg.role.admin.audit.enums.AuthorUserClassDesignator;
import com.hmg.role.admin.audit.exceptions.BrokenAuditEntryException;
import com.hmg.role.admin.audit.exceptions.NoSuchCommitException;
import com.hmg.role.admin.audit.interfaces.AuditService;
import com.hmg.role.sdk.common.util.Utils;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.container.Pair;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.enums.PermissionStructure;
import com.hmg.role.util.exceptions.BadDateTimeException;
import com.hmg.role.util.exceptions.InternalServerErrorException;
import com.hmg.role.util.exceptions.TypeNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.commit.CommitId;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    private final Javers javers;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private RequestAuditContext ctx;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    @Autowired
    public AuditServiceImpl(Javers javers, RequestAuditContext ctx) {
        this.javers = javers;
        this.ctx = ctx;
    }

    @Override
    public void commitAsync(Object entityObject) {
        var author = getAuthor();
        var authorId = author.second();

        var thisScope = ctx.copy();
        var userScopeKey = authorRequestScope.getDefaultScopeRbac().get().getKey();

        // append metadata manually based on captured data by AuditInterceptor
        // there's probably a cleaner way, but for now this will do
        if (entityObject instanceof List<?> entities) {
            entities.forEach(
                    entity ->
                            commitJavers(
                                    entity,
                                    authorId,
                                    generateMetadata(thisScope, author, entity, userScopeKey)));
        } else if (entityObject instanceof Collection<?> entities) {
            entities.forEach(
                    entity ->
                            commitJavers(
                                    entity,
                                    authorId,
                                    generateMetadata(thisScope, author, entity, userScopeKey)));
        } else {
            commitJavers(
                    entityObject,
                    authorId,
                    generateMetadata(thisScope, author, entityObject, userScopeKey));
        }
    }

    @Override
    public ListResponseDto<AuditTrailResponseDto> getAuditTrails(
            HttpServletRequest request, AuditTrailsRequestDto filters) {
        // validate dates
        final var startDateStr = filters.getStartDate();
        ZonedDateTime startDate;
        try {
            startDate = Utils.parseIsoDate(startDateStr);
        } catch (DateTimeParseException e) {
            log.warn("Error parsing start date, message: {}", e.getMessage(), e);
            throw new BadDateTimeException();
        }
        // Don't let start date invalid or be earlier than 3 months from today
        final var today = ZonedDateTime.now();
        validateStartDate(startDate, today);
        // since there's no hour information given
        startDate = startDate.minusDays(1);

        var endDate = today;
        final var endDateStr = filters.getEndDate();
        if (!StringUtils.isBlank(endDateStr)) {
            try {
                endDate = Utils.parseIsoDate(endDateStr);
            } catch (DateTimeParseException e) {
                log.warn("Error parsing end date, message: {}", e.getMessage(), e);
                throw new BadDateTimeException();
            }
        }
        // since there's no hour information given
        endDate = endDate.plusDays(1);

        Class<?>[] allClasses = new Class<?>[AUDIT_PATH_TO_ENTITY_MAP.size()];
        allClasses = AUDIT_PATH_TO_ENTITY_MAP.values().toArray(allClasses);

        final String entityPath = filters.getEntityPath();
        QueryBuilder qb;
        if (StringUtils.isBlank(entityPath)
                || ALL_ENTITY_PATH_IDENTIFIER.equalsIgnoreCase(entityPath)) {
            // WARN: potential performance issue
            // show all audited entity classes
            qb =
                    QueryBuilder.byClass(allClasses)
                            // but filter out those with NULL keys
                            .withCommitPropertyLike(AUDIT_ATTR_ENTITY_KEY, "");
        } else if (StringUtils.equalsIgnoreCase(entityPath, RBAC_ENTITY_PATH_IDENTIFIER)) {
            qb =
                    QueryBuilder.byClass(allClasses)
                            .withCommitPropertyLike(
                                    AUDIT_ATTR_ENTITY_PATH,
                                    PermissionStructure.RBAC.name().toLowerCase());
        } else {
            validateEntityPath(entityPath);

            Class<?> entityClass = findClass(entityPath);
            qb = QueryBuilder.byClass(entityClass);
        }

        String projectKey;
        var viewingAuthor = getAuthor();
        if (viewingAuthor.first() == USER_ID) { // user logged in from front end
            // limit by project or scope
            projectKey = authorRequestScope.getProject().getKey();
        } else { // user used API
            projectKey = filters.getProjectKey();
        }

        if (!StringUtils.isBlank(projectKey)) {
            qb = qb.withCommitProperty(AUDIT_ATTR_ENTITY_PROJECT_KEY, projectKey);
        }
        var scopeKey = filters.getScopeKey();
        if (!StringUtils.isBlank(scopeKey)) {
            qb = qb.withCommitProperty(AUDIT_ATTR_SCOPE_KEY, scopeKey);
        }

        var changesAuthor = filters.getAuthor();
        if (!StringUtils.isBlank(changesAuthor)) {
            qb = qb.byAuthorLikeIgnoreCase(changesAuthor);
        }

        var entityKey = filters.getKey();
        if (!StringUtils.isBlank(entityKey)) {
            qb.withCommitPropertyLike(AUDIT_ATTR_ENTITY_KEY, entityKey);
        }

        var clientIp = filters.getIp();
        if (!StringUtils.isBlank(clientIp)) {
            qb = qb.withCommitProperty(AUDIT_ATTR_USER_IP, clientIp);
        }
        // WARN: blank author, key, or IP might cause performance issue since all snapshots will be
        // fetched

        // date limit
        qb = qb.fromInstant(startDate.toInstant()).toInstant(endDate.toInstant());

        // Javers SQL default limit is 100; Integer.MAX_VALUE overrides it to fetch all results
        // Javers limit()/skip() cannot return a total count, so manual in-memory slicing is used
        // instead
        // WARN: potential performance issue: all matching snapshots loaded into memory before
        // pagination
        // TODO: add db indices for performance
        var changesByCommit =
                javers.findChanges(qb.limit(Integer.MAX_VALUE).build()).groupByCommit();

        // Javers QueryBuilder has no escape-char support, so _ and % are SQL wildcards in its
        // generated LIKE clauses. Post-filter in Java for exact contains semantics.
        if (StringUtils.isNotBlank(changesAuthor) || StringUtils.isNotBlank(entityKey)) {
            changesByCommit =
                    changesByCommit.stream()
                            .filter(
                                    cg -> {
                                        var c = cg.getCommit();
                                        if (StringUtils.isNotBlank(changesAuthor)) {
                                            var author = c.getAuthor();
                                            if (author == null
                                                    || !author.toLowerCase()
                                                            .contains(changesAuthor.toLowerCase()))
                                                return false;
                                        }
                                        if (StringUtils.isNotBlank(entityKey)) {
                                            var k = c.getProperties().get(AUDIT_ATTR_ENTITY_KEY);
                                            if (k == null
                                                    || !k.toLowerCase()
                                                            .contains(entityKey.toLowerCase()))
                                                return false;
                                        }
                                        return true;
                                    })
                            .toList();
        }

        var res = convertChangesToPaginatedDto(changesByCommit, filters);
        return ListResponseDto.create(res);
    }

    @Override
    public AuditTrailDetailsResponseDto getAuditTrailDetails(
            AuditTrailDetailsRequestDto detailRequest) {
        String entityPath = detailRequest.entityPath();
        validateEntityPath(entityPath);
        var entityClass = findClass(entityPath);

        var id = CommitId.valueOf(detailRequest.commitId());
        var q = QueryBuilder.byClass(entityClass).withCommitId(id).build();

        var queryResult = javers.findShadows(q);
        if (queryResult.isEmpty()) {
            log.info("invalid commit id: {}, class: {}", id, entityClass);
            throw new NoSuchCommitException();
        }

        if (queryResult.size() > 1) {
            // (commitId, class) tuple should yield unique shadow. This is unexpected
            log.error(
                    "Unexpected audit entry, got multiple for commitId: {} and class: {}",
                    id,
                    entityClass.getName());
            throw new BrokenAuditEntryException();
        }

        final var result = queryResult.getFirst();

        String snapshotStr;
        try {
            snapshotStr = shallowSerToStr(result.get());
        } catch (JsonProcessingException e) {
            log.error("Error serializing snapshot", e);
            throw new InternalServerErrorException("Error when parsing snapshot");
        }

        return AuditTrailDetailsResponseDto.builder().snapshot(snapshotStr).build();
    }

    @Override
    public AuditFilterResponseDto getAuditTrailFilters() {
        List<String> auditPaths = getAuditEntityPaths();
        return AuditFilterResponseDto.builder().keyTypes(valuesAsList()).paths(auditPaths).build();
    }

    private static List<String> getAuditEntityPaths() {
        List<String> auditPaths = new ArrayList<>(AUDIT_ENTITY_PATHS);
        auditPaths.add(RBAC_ENTITY_PATH_IDENTIFIER);
        auditPaths.add(ALL_ENTITY_PATH_IDENTIFIER);
        return auditPaths;
    }

    private Pair<AuthorUserClassDesignator, String> getAuthor() {
        if (authorRequestScope.getHmgAdminUserInfo() != null) {
            return new Pair<>(USER_ID, authorRequestScope.getHmgAdminUserInfo().getUserId());
        }
        return new Pair<>(AuthorUserClassDesignator.MEMBER_KEY, authorRequestScope.getMemberKey());
    }

    private void commitJavers(Object entity, String authorId, Map<String, String> metadata) {
        Object snapshot = snapshotEntity(entity);
        if (metadata != null && !metadata.isEmpty()) {
            executorService.execute(() -> javers.commit(authorId, snapshot, metadata));
        } else {
            executorService.execute(() -> javers.commit(authorId, snapshot));
        }
    }

    private Object snapshotEntity(Object entity) {
        try {
            Object copy = entity.getClass().getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, copy);
            return copy;
        } catch (Exception e) {
            log.warn(
                    "Failed to snapshot entity [{}] for audit commit, using live reference — risk of race condition on delete",
                    entity.getClass().getSimpleName(),
                    e);
            return entity;
        }
    }

    private static Map<String, String> generateMetadata(
            RequestAuditContext ctx,
            Pair<AuthorUserClassDesignator, String> author,
            Object entityObject,
            String entityScopeKey) {

        if (!isEntityMetadataRecorded(entityObject)) {
            return null; // NOSONAR: javers will break with empty map
        }

        var permissionStructure = getPermissionStructure(ctx.getRequestPath(), entityObject);

        var entityId = getEntityId(entityObject);
        var entityKey
                // append in metadata since user wants to search by key
                = getEntityKey(entityObject);
        var entityProjectKey = getEntityProjectKey(entityObject);
        var clientIp = Optional.ofNullable(ctx.getClientIp()).orElse(NOT_AVAILABLE);
        var userAgent = Optional.ofNullable(ctx.getUserAgent()).orElse(NOT_AVAILABLE);
        var httpRequestMethod = getMethod(ctx, entityObject).orElse(NOT_AVAILABLE);
        var httpPath = Optional.ofNullable(ctx.getRequestPath()).orElse(NOT_AVAILABLE);

        if (entityKey == null) {
            log.warn("entityKey is null for requestId: {}", ctx.getRequestId());
            entityKey = NOT_AVAILABLE;
        }
        if (entityProjectKey == null) {
            log.warn("entityProjectKey is null for requestId: {}", ctx.getRequestId());
            entityProjectKey = NOT_AVAILABLE;
        }

        String entityPath = getEntityPath(entityObject);

        return AuditUtils.wrapMetadata(
                ctx,
                author,
                entityScopeKey,
                permissionStructure,
                entityId,
                entityKey,
                entityPath,
                entityProjectKey,
                clientIp,
                userAgent,
                httpRequestMethod,
                httpPath);
    }

    void validateEntityPath(String path) {
        if (!AUDIT_ENTITY_PATHS.contains(path)) {
            log.info("Invalid entity path: {}", path);
            throw new TypeNotFoundException();
        }
    }
}
