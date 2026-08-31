package com.hmg.role.common.config.interceptor;

import com.hmg.role.admin.audit.AuditUtils;
import com.hmg.role.admin.audit.RequestAuditContext;
import com.hmg.role.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class AuditInterceptor implements HandlerInterceptor {

    private final RequestAuditContext ctx;

    public AuditInterceptor(RequestAuditContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {

        ctx.setHttpRequestMethod(request.getMethod());
        ctx.setRequestPath(request.getRequestURI());
        // probably necessary to store user agent as well, jot it down for now
        ctx.setUserAgent(request.getHeader("User-Agent"));
        ctx.setRequestId(MDC.get(Constants.MDC_KEY_TRACE_ID));
        ctx.setClientIp(AuditUtils.resolveClientIp(request));
        return true;
    }
}
