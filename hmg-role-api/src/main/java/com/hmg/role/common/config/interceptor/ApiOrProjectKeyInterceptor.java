package com.hmg.role.common.config.interceptor;

import static com.hmg.role.util.Constants.X_HMG_ROLE_API_KEY;
import static com.hmg.role.util.Constants.X_HMG_ROLE_PROJECT_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hmg.admin.model.UserInfoVo;
import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.admin.member.Member;
import com.hmg.role.admin.member.MemberRepository;
import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.ProjectRepository;
import com.hmg.role.admin.project.exceptions.ProjectNotFoundException;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.Cache;
import com.hmg.role.util.Constants;
import com.hmg.role.util.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Slf4j
public class ApiOrProjectKeyInterceptor implements HandlerInterceptor {

    private static final Path ADMIN_API_PATH = Path.of("/api/admin");
    private static final Path ERROR_PATH = Path.of("/api/error");

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    private static final String HTTP_OPTIONS = "OPTIONS";

    @Autowired private ObjectMapper objectMapper;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    // TODO make this immutable and use factory method
    private AuthorRequestScope authorRequestScope;

    @Value("${hmg-role.admin.api-key}")
    private String adminApiKey;

    @Value("${hmg-role.admin.name}")
    private String adminName;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (HTTP_OPTIONS.equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        var reqUriPath = Path.of(request.getRequestURI());
        if (reqUriPath.startsWith(ERROR_PATH)) {
            return true;
        }

        if (hasValidApiKey(request, reqUriPath)) {
            return true;
        } else if (hasValidJwtAndProjectKey(request, reqUriPath)) {
            return true;
        }
        // TODO move authentication/authorization step to Spring Security
        throw new UnauthorizedException();
    }

    private boolean hasValidApiKey(HttpServletRequest request, Path requestUriPath) {
        // TODO separate AuthorRequestScope operand assignment and API checking

        var apiKey = request.getHeader(X_HMG_ROLE_API_KEY);
        if (!StringUtils.hasText(apiKey)) {
            return false;
        }
        var isAdminApiPathRequested = requestUriPath.startsWith(ADMIN_API_PATH);
        if (isAdminApiPathRequested) {
            log.debug("'{}' is requested", ADMIN_API_PATH);
            if (adminApiKey.equals(apiKey)) {
                log.debug("ApiKey: '{}' is a valid admin key", apiKey);
                // TODO implement hmg-role as project and its admin as member in db (not in config
                // yml)
                var member = new Member();
                member.setId(0L);
                member.setKey(adminName);
                Project project = new Project();
                project.setId(0L);
                project.setKey("hmg-role");
                member.setProject(project);
                authorRequestScope.setMember(member);
                Scope scope = new Scope();
                scope.setProject(project);
                scope.setScopeId(0L);
                scope.setKey(Constants.DEFAULT_SCOPE_KEY);
                authorRequestScope.setDefaultScopeRbac(new Cache<>(() -> scope));
                AbacScope abacScope = new AbacScope();
                abacScope.setProject(project);
                abacScope.setId(0L);
                abacScope.setKey(Constants.DEFAULT_SCOPE_KEY);
                authorRequestScope.setDefaultScopeAbac(new Cache<>(() -> abacScope));

                MDC.put(Constants.MDC_KEY_PROJECT_KEY, "hmg-role");
                MDC.put(Constants.MDC_KEY_MEMBER_KEY, "admin-key");
                request.setAttribute(Constants.MDC_KEY_PROJECT_KEY, "hmg-role");
                request.setAttribute(Constants.MDC_KEY_MEMBER_KEY, "admin-key");
            } else if (exemptedRequestPath(requestUriPath)) {
                // TODO refactor this later
                log.info("exempted URI path is requested: '{}'", requestUriPath);
                setMemberToRequestScope(apiKey, request);
            } else {
                // admin api can only be accessed using adminApiKey
                throw new UnauthorizedException("admin api can only be accessed using adminApiKey");
            }
        } else {
            setMemberToRequestScope(apiKey, request);
        }
        return true;
    }

    private boolean hasValidJwtAndProjectKey(HttpServletRequest request, Path requestUriPath) {
        // TODO: separate checking and side effects application

        var projectKey = request.getHeader(X_HMG_ROLE_PROJECT_KEY);

        if (!StringUtils.hasText(projectKey)) {
            log.debug("Header {} is not found", X_HMG_ROLE_PROJECT_KEY);
            return false;
        }

        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();

        var userInfoVo = new UserInfoVo();
        var isAdminApiPathRequested = requestUriPath.startsWith(ADMIN_API_PATH);
        if (isAdminApiPathRequested) {
            var project =
                    projectRepository
                            .findByKeyAndDeletedFalse(projectKey)
                            .orElseThrow(
                                    () ->
                                            new UnauthorizedException(
                                                    "ProjectKey is not found in database."));
            log.info("User Info : {}", userInfoVo);
            authorRequestScope.setHmgAdminUserInfo(userInfoVo);
            authorRequestScope.setProject(project);
            // keep it like this
            // this intereptor will have detached hibernate session
            // and thus it will throw "session closed" errors
            // if it is lazily initialized and the cache then later be reused
            authorRequestScope.setDefaultScopeRbac(
                    new Cache<>(() -> projectRepository.getDefaultScopeRbac(project)));
            authorRequestScope.setDefaultScopeAbac(
                    new Cache<>(() -> projectRepository.getDefaultScopeAbac(project)));
        } else {
            var project =
                    projectRepository
                            .findByKeyAndDeletedFalse(projectKey)
                            .orElseThrow(
                                    () ->
                                            new UnauthorizedException(
                                                    "ProjectKey is not found in database."));

            log.info("User Info : {}", userInfoVo);
            authorRequestScope.setHmgAdminUserInfo(userInfoVo);
            authorRequestScope.setProject(project);
            // keep it like this
            // this intereptor will have detached hibernate session
            // and thus it will throw "session closed" errors
            // if it is lazily initialized and the cache then later be reused
            authorRequestScope.setDefaultScopeRbac(
                    new Cache<>(() -> projectRepository.getDefaultScopeRbac(project)));
            authorRequestScope.setDefaultScopeAbac(
                    new Cache<>(() -> projectRepository.getDefaultScopeAbac(project)));
        }

        var userId = userInfoVo.getUserId();

        MDC.put(Constants.MDC_KEY_PROJECT_KEY, projectKey);
        MDC.put(Constants.MDC_KEY_MEMBER_KEY, userId);
        request.setAttribute(Constants.MDC_KEY_PROJECT_KEY, projectKey);
        request.setAttribute(Constants.MDC_KEY_MEMBER_KEY, userId);

        return true;
    }

    private static String quote(String s) {
        return "'" + s + "'";
    }

    private static boolean exemptedRequestPath(Path reqPath) {
        String reqPathStr = reqPath.toString();
        // windows messed up the separator :( should've gotten linux dev laptops instead
        String[] paths = reqPathStr.replace("\\", "/").split("/");
        // should contain '/api/admin/{version}/{theTarget}'
        // the first element is blank (since there's preceeding slash), so take the n+1th element
        String targetedReqPath = paths[4];
        return Constants.EXEMPTED_REQUEST_PATH_ENDPOINT.contains(targetedReqPath);
    }

    private void setMemberToRequestScope(String apiKey, HttpServletRequest request) {
        var member =
                memberRepository
                        .findWithProjectByApiKey(apiKey)
                        .orElseThrow(() -> ProjectNotFoundException.ofApiKey(apiKey));

        String memberProjectKey = member.getProject().getKey();
        var projectKey =
                Optional.ofNullable(request.getHeader(X_HMG_ROLE_PROJECT_KEY))
                        .orElse(memberProjectKey);
        var project = projectRepository.findByKeyAndDeletedFalse(projectKey);

        var projectEntity =
                project.orElseThrow(
                        () -> new UnauthorizedException("ProjectKey is not found in database."));

        authorRequestScope.setMember(member);
        authorRequestScope.setProject(projectEntity);
        // keep it like this
        // this intereptor will have detached hibernate session
        // and thus it will throw "session closed" errors
        // if it is lazily initialized and the cache then later be reused
        authorRequestScope.setDefaultScopeRbac(
                new Cache<>(() -> projectRepository.getDefaultScopeRbac(projectEntity)));
        authorRequestScope.setDefaultScopeAbac(
                new Cache<>(() -> projectRepository.getDefaultScopeAbac(projectEntity)));

        MDC.put(Constants.MDC_KEY_PROJECT_KEY, projectKey);
        MDC.put(Constants.MDC_KEY_MEMBER_KEY, member.getKey());
        request.setAttribute(Constants.MDC_KEY_PROJECT_KEY, projectKey);
        request.setAttribute(Constants.MDC_KEY_MEMBER_KEY, member.getKey());
        log.debug("ApiKey: '{}' is a valid member from projectKey: {}", apiKey, memberProjectKey);
    }
}
