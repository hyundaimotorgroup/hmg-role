package com.hmg.role.common.config.interceptor;

import com.hmg.role.admin.project.Project;
import com.hmg.role.admin.project.enums.OperatingCountry;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class UserApiInterceptor implements HandlerInterceptor {
    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        Project project = authorRequestScope.getProject();

        boolean isPersonalDataSelfHandled = project.isPersonalDataSelfHandled();
        var userInfo = authorRequestScope.getHmgAdminUserInfo();

        var projectOperatingCountry = project.getOperatingCountry();
        if (StringUtils.equalsIgnoreCase(projectOperatingCountry, OperatingCountry.KOREA.value)) {
            String serviceConsentDoc = project.getServiceConsentHistoryUrl();
            boolean userIsUseValidIp = userInfo.isUserSettingIp();

            boolean grantAccess;
            if (isPersonalDataSelfHandled) {
                grantAccess = userIsUseValidIp && !StringUtils.isBlank(serviceConsentDoc);
            } else {
                // user's project doesn't handle personal data;
                // based on the requirement, grant the access
                grantAccess = true;
            }

            if (grantAccess) {
                log.info(
                        "user: {} from project: {} was granted authorization",
                        authorRequestScope.getMemberKey(),
                        project.getKey());
                return true;
            } else {
                log.error(
                        "user: {} from project: {} was denied authorization",
                        authorRequestScope.getMemberKey(),
                        project.getKey());
                throw new UnauthorizedException();
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
