package com.hmg.role.common.config;

import static com.hmg.role.util.Constants.X_HMG_ROLE_API_KEY;
import static com.hmg.role.util.Constants.X_HMG_ROLE_PROJECT_KEY;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(X_HMG_ROLE_API_KEY))
                .addSecurityItem(new SecurityRequirement().addList(X_HMG_ROLE_PROJECT_KEY))
                .components(
                        new Components()
                                .addSecuritySchemes(X_HMG_ROLE_API_KEY, createAPIKeyScheme())
                                .addSecuritySchemes(
                                        X_HMG_ROLE_PROJECT_KEY, createProjectKeyScheme()))
                .info(
                        new Info()
                                .title("HMG Role")
                                .description("HMG Role API Documentation.")
                                .version("0.1"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .name(X_HMG_ROLE_API_KEY)
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .description("API Key Authorization");
    }

    private SecurityScheme createProjectKeyScheme() {
        return new SecurityScheme()
                .name(X_HMG_ROLE_PROJECT_KEY)
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .description("Project Key Authorization");
    }

    @Bean
    public GroupedOpenApi groupedRbacPolicyApi() {
        return GroupedOpenApi.builder()
                .group("RBAC Policy API")
                .pathsToMatch(
                        "/api/rbac/v1/**",
                        "/api/rbac/v1/users/**",
                        "/api/rbac/v1/roles/**",
                        "/api/rbac/v1/resource-types/**",
                        "/api/rbac/v1/permissions/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupedAbacPolicyApi() {
        return GroupedOpenApi.builder()
                .group("ABAC Policy API")
                .pathsToMatch(
                        "/api/abac/**",
                        "/api/abac/v1/user-sets/**",
                        "/api/abac/v1/user-set-attributes/**",
                        "/api/abac/v1/resource-sets/**",
                        "/api/abac/v1/policies/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupedAdminApi() {
        return GroupedOpenApi.builder().group("Admin API").pathsToMatch("/api/admin/**").build();
    }
}
