package com.hmg.role.admin.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter
@Setter
public class RequestAuditContext {
    private String clientIp;
    private String httpRequestMethod;
    private String requestPath;
    private String userAgent;
    private String requestId;

    public boolean hasData() {
        return httpRequestMethod != null && requestPath != null;
    }

    public synchronized RequestAuditContext copy() {
        // necessary since this context holder is mutable and will be used in threaded context
        // TODO use something like AuditContextFactory in the interceptor
        RequestAuditContext c = new RequestAuditContext();
        c.clientIp = this.clientIp;
        c.httpRequestMethod = this.httpRequestMethod;
        c.requestPath = this.requestPath;
        c.userAgent = this.userAgent;
        c.requestId = this.requestId;
        return c;
    }
}
