package com.hmg.role.common.config.interceptor;

import com.hmg.role.admin.audit.AuditConstants;
import com.hmg.role.admin.audit.exceptions.AuditPermissionMissingException;
import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.enums.OperatingCountry;
import com.hmg.role.util.AuthorRequestScope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class AuditTrailApiInterceptor implements HandlerInterceptor {
    private static final String UNAUTHORIZED_LOG_MESSAGE_PATTERN =
            "member: {} from project: {} was denied authorization."
                    + " country: {},  validIp: {} (should be true)";

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    private static final String HTTP_OPTIONS = "OPTIONS";

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        // for CORS preflight request
        // to avoid CORS error during local testing
        if (HTTP_OPTIONS.equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Project project = authorRequestScope.getProject();

        var userInfo = authorRequestScope.getHmgAdminUserInfo();

        var siteCode = project.getHmgAdminModuleCode();

        if (userInfo == null) {
            throw AuditPermissionMissingException.ofNoUserInfoVo();
        }

        // check if user is allowed to read audit trail page
        // from hmgAdmin
        log.info(
                "User site permissions : {} ",
                String.join(",", userInfo.getSitePermissions(siteCode)));
        boolean auditTrailReadPermission =
                userInfo.anyPermissions(siteCode, AuditConstants.AUDIT_TRAIL_READ_PERMISSIONS);
        log.info(" auditTrailReadPermission : {} ", auditTrailReadPermission);
        if (!auditTrailReadPermission) {
            throw new AuditPermissionMissingException();
        }

        var projectOperatingCountry = project.getOperatingCountry();
        if (StringUtils.equalsIgnoreCase(projectOperatingCountry, OperatingCountry.KOREA.value)) {
            boolean userIsUseValidIp = userInfo.isUserSettingIp();

            // only allow access
            // for members based on HMG-Admin SDK IP Access List
            if (userIsUseValidIp) {
                log.info(
                        "member: {} from project: {} was granted authorization",
                        authorRequestScope.getMemberKey(),
                        project.getKey());
                return true;
            } else {
                log.error(
                        UNAUTHORIZED_LOG_MESSAGE_PATTERN,
                        authorRequestScope.getMemberKey(),
                        project.getKey(),
                        projectOperatingCountry,
                        userIsUseValidIp);
                throw new AuditPermissionMissingException();
            }
        } else {
            log.info(
                    "user: {} from project: {} is from outside Korea",
                    authorRequestScope.getMemberKey(),
                    project.getKey());
            // grant permission
            // TODO verify
            return true;
        }
    }
}
