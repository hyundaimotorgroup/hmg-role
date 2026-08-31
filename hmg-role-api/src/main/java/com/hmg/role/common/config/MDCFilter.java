package com.hmg.role.common.config;

import com.hmg.role.admin.audit.AuditUtils;
import com.hmg.role.util.Constants;
import jakarta.servlet.*;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MDCFilter implements Filter {

    // This MDCFilter Code is not support Asynchronous process.
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate traceId once
        String traceId = UUID.randomUUID().toString();

        // Put traceId in MDC context
        MDC.put(Constants.MDC_KEY_TRACE_ID, traceId);

        MDC.put(Constants.MDC_KEY_API_KEY, httpRequest.getHeader(Constants.X_HMG_ROLE_API_KEY));
        MDC.put(Constants.MDC_KEY_USER_IP, AuditUtils.resolveClientIp(httpRequest));

        // Set traceId in custom header
        httpResponse.setHeader("X-HMG-ROLE-TRACE-ID", traceId);

        chain.doFilter(request, response);
    }
}
