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

public class CreateRoleStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("the Resource Admin has the following API key: {string}")
    public void setApiKey(String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
    }

    @Given("Resource Admin wants to create a new Role with the following attributes")
    public void setRequest(DocString request) {
        scenarioContext.setContext("requestForCreateRole", request.getContent());
    }

    @When("the Resource Admin creates a new Role")
    public void createNewRole() {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        var response = createRole(apiKey);
        scenarioContext.setContext("createRoleResponse", response);
    }

    private Map createRole(String apiKey) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestForCreateRole"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .post("/api/rbac/v1/roles");
        scenarioContext.setContext("createRoleResponseCode", response.statusCode());
        return response.thenReturn().as(HashMap.class);
    }

    @Then("the Role is created successfully with response code {int}")
    public void checkCreateSuccess(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("createRoleResponseCode"))
                .isEqualTo(expectedResponseCode);
    }

    @And("return the created Role")
    public void checkCreatedResponseBody(DocString expectedResponseBody) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("createRoleResponse");

        assertThat(result.get("name")).isEqualTo(expectedResponseMap.get("name"));
        assertThat(result.get("description")).isEqualTo(expectedResponseMap.get("description"));
    }

    @Given("Resource Admin wants to create a new Role with an empty name")
    public void setRequestWithEmptyName(DocString request) {
        scenarioContext.setContext("requestForCreateRole", request.getContent());
    }

    @Then("the Role creation fails with response code {int}")
    public void checkResponseCodeCreateFailed(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("createRoleResponseCode"))
                .isEqualTo(expectedResponseCode);
    }

    @Given("Resource Admin wants to create a new Role with a null key")
    public void setRequestWithNullKey(DocString request) {
        scenarioContext.setContext("requestForCreateRole", request.getContent());
    }

    @Given("an existing Role from the project A with API key {string} as the following attributes")
    public void createNewRoleBackground(String apiKey, DocString request) {
        scenarioContext.setContext("requestForCreateRole", request.getContent());
        var result = createRole(apiKey);
        scenarioContext.setContext("createRoleResponse", result);
    }

    @When(
            "the Resource Admin from the project B with API key {string} wants to Create a new Role with the same name")
    public void createRoleWithSameName(String apiKey, DocString request) {
        scenarioContext.setContext("requestForCreateRole", request.getContent());
        var result = createRole(apiKey);
        scenarioContext.setContext("createRoleResponse", result);
    }

    @And("the create role response body includes status {int} and error message {string}")
    public void checkFailedCreateResponseBody(int statusCode, String errorMessage) {
        var result = (Map) scenarioContext.getContext("createRoleResponse");
        assertThat(result.get("status")).isEqualTo(statusCode);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }

    @When(
            "the Resource Admin wants to Create a new Role with API key {string} as the following attributes")
    public void createDuplicateRole(String apiKey, DocString request) {
        scenarioContext.setContext("requestForCreateRole", request.getContent());
        var result = createRole(apiKey);
        scenarioContext.setContext("createRoleResponse", result);
    }
}
