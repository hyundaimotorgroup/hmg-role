package com.hmg.role.rbac.permission;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.restassured.http.ContentType;

public class PermissionCheckingStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("Permission Checking Step")
    public void PermissionCheckingStep() {}

    @And("a Role for API key {string} with specific attributes exists")
    public void createRoleForTestBackground(String apiKey, DocString request) {
        request()
                .log()
                .all()
                .contentType(ContentType.JSON)
                .body(request.getContent())
                .headers("X-HMG-ROLE-API-KEY", apiKey)
                .post("/api/rbac/v1/roles");
    }

    @And("a User for API key {string} with specific attributes exists")
    public void createUserForTestBackground(String apiKey, DocString request) {
        scenarioContext.setContext("apiKey", apiKey);
        request()
                .log()
                .all()
                .contentType(ContentType.JSON)
                .body(request.getContent())
                .headers("X-HMG-ROLE-API-KEY", apiKey)
                .post("/api/rbac/v1/users");
    }

    @And("a Member with specific attributes exists")
    public void createMemberForTestBackground(DocString request) {
        var apiKey = scenarioContext.getContext("apiKeyProjectAdmin");
        var projectKey = scenarioContext.getContext("projectKey");
        request()
                .log()
                .all()
                .contentType(ContentType.JSON)
                .body(request.getContent())
                .headers("X-HMG-ROLE-API-KEY", apiKey)
                .post("/api/admin/v1/projects/{projectKey}/members", projectKey);
    }
}
