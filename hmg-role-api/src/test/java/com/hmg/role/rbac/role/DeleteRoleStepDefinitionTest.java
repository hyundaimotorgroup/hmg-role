package com.hmg.role.rbac.role;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public class DeleteRoleStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("the Resource Admin has the following API name: {string}")
    public void setApiKey(String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
    }

    @And("a Role with the following attributes exists")
    public void createARoleBackground(DocString request) {
        scenarioContext.setContext("requestForCreateRole", request.getContent());
        var apiKey = (String) scenarioContext.getContext("apiKey");
        createRole(apiKey);
    }

    @When("Resource Admin wants to delete the Role with key {string} and API key {string}")
    public void setRoleKeyAndApiKey(String key, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @Then("the Role deleted successfully with response code {int}")
    public void checkDeletedRoleSuccess(int expectedResponseCode) {
        deleteRole();
        assertThat(scenarioContext.getContext("responseCodeForDeleteRole"))
                .isEqualTo(expectedResponseCode);
    }

    private void deleteRole() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete("/api/rbac/v1/roles/{key}", key);
        scenarioContext.setContext("responseCodeForDeleteRole", response.statusCode());
        if (response.statusCode() != 204) {
            var result = response.thenReturn().as(HashMap.class);
            scenarioContext.setContext("errorResponseForDeleteRole", result);
        }
    }

    @Then("the Role deletion fails with response code {int}")
    public void checkDeleteRoleFailed(int expectedResponseCode) {
        deleteRole();
        assertThat(scenarioContext.getContext("responseCodeForDeleteRole"))
                .isEqualTo(expectedResponseCode);
    }

    @And(
            "the response body for deleting a role, including the status code {int} and error message {string}")
    public void checkResponseBodyForDeleteRoleFailed(int statusCode, String errorMessage) {
        var result = (Map) scenarioContext.getContext("errorResponseForDeleteRole");
        assertThat(result.get("status")).isEqualTo(statusCode);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }

    @And("a User as the following attributes exists")
    public void createUserBackground(DocString request) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(request.getContent())
                        .headers("X-HMG-ROLE-API-KEY", scenarioContext.getContext("apiKey"))
                        .post("/api/rbac/v1/users");
        assertThat(response.statusCode()).isEqualTo(201);
    }

    private void createRole(String apiKey) {
        request()
                .log()
                .all()
                .contentType(ContentType.JSON)
                .body(scenarioContext.getContext("requestForCreateRole"))
                .headers("X-HMG-ROLE-API-KEY", apiKey)
                .post("/api/rbac/v1/roles");
    }
}
