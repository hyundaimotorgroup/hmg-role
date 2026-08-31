package com.hmg.role.common.config;

import com.hmg.role.admin.audit.RequestAuditContext;
import com.hmg.role.admin.member.MemberRepository;
import com.hmg.role.admin.project.ProjectRepository;
import com.hmg.role.common.config.interceptor.ApiOrProjectKeyInterceptor;
import com.hmg.role.common.config.interceptor.AuditInterceptor;
import com.hmg.role.common.config.interceptor.AuditTrailApiInterceptor;
import com.hmg.role.common.config.interceptor.UserApiInterceptor;
import com.hmg.role.util.AuthorRequestScope;
import io.micrometer.common.util.StringUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final RequestAuditContext requestAuditContext;

    @Value("${frontend-domain}")
    private String frontEndDomain;

    @Value("${backend-domain}")
    private String backendDomain;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        var excludePaths =
                List.of(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/api/swagger-ui/**");

        var includePaths = List.of("/api/admin/v1/**", "/api/rbac/**", "/api/abac/**");

        var auditTrailPath = "/api/admin/**/audit-trails/**";
        var userPath = "**/api/users/**";

        registry.addInterceptor(apiOrProjectKeyInterceptor())
                .excludePathPatterns(excludePaths)
                .addPathPatterns(includePaths);

        registry.addInterceptor(auditInterceptor()).addPathPatterns(includePaths);

        registry.addInterceptor(auditTrailApiInterceptor()).addPathPatterns(auditTrailPath);

        registry.addInterceptor(userApiInterceptor()).addPathPatterns(userPath);
    }

    @Bean
    ApiOrProjectKeyInterceptor apiOrProjectKeyInterceptor() {
        return new ApiOrProjectKeyInterceptor(memberRepository, projectRepository);
    }

    @Bean
    AuditInterceptor auditInterceptor() {
        return new AuditInterceptor(requestAuditContext);
    }

    @Bean
    AuditTrailApiInterceptor auditTrailApiInterceptor() {
        return new AuditTrailApiInterceptor();
    }

    @Bean
    UserApiInterceptor userApiInterceptor() {
        return new UserApiInterceptor();
    }

    @Bean
    @RequestScope
    AuthorRequestScope authorRequestScope() {
        return new AuthorRequestScope();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!StringUtils.isEmpty(frontEndDomain) && !StringUtils.isEmpty(backendDomain)) {
            registry.addMapping("/**")
                    .allowedOrigins(frontEndDomain, backendDomain)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        }
    }
}
