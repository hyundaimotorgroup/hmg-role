package com.hmg.role.rbac.user;

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

public class CreateUserStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("Project Admin wants to create a new User with the following attributes")
    public void setCreateUserRequestBody(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @When("the Project Admin creates a new User")
    public void createNewUser() {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        var response = createUser(apiKey);
        scenarioContext.setContext("createUserResponse", response);
    }

    private Map createUser(String apiKey) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("createUserRequestBody"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .post("/api/rbac/v1/users");
        scenarioContext.setContext("createUserResponseCode", response.statusCode());
        return response.thenReturn().as(HashMap.class);
    }

    @Then("the User is created successfully with response code {int}")
    public void checkResponseCodeForCreatedUser(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("createUserResponseCode"))
                .isEqualTo(expectedResponseCode);
    }

    @And("return the created User")
    public void checkResponseBodyForCreatedUser(DocString expectedResponseBody) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("createUserResponse");

        assertThat(result.get("id")).isEqualTo(expectedResponseMap.get("id"));
        assertThat(result.get("name")).isEqualTo(expectedResponseMap.get("name"));
        assertThat(result.get("roles")).isEqualTo(expectedResponseMap.get("roles"));
    }

    @Given("Project Admin wants to create a new User with an empty name")
    public void setRequestWithEmptyName(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Given("Project Admin wants to create a new User with a null name")
    public void setRequestWithNullName(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Given("Project Admin wants to create a new User with a blank name")
    public void setRequestWithBlankName(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Given("Project Admin wants to create a new User with an empty key")
    public void setRequestWithEmptyId(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Then("the User creation fails with response code {int}")
    public void checkResponseCodeForFailedCreationUser(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("createUserResponseCode"))
                .isEqualTo(expectedResponseCode);
    }

    @Given("Project Admin wants to create a new User with a null key")
    public void setRequestWithNullId(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Given("Project Admin wants to create a new User with a blank key")
    public void setRequestWithBlankId(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Given("Project Admin wants to create a new User with an empty roles")
    public void setRequestWithEmptyRoles(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Given("Project Admin wants to create a new User with a null roles")
    public void setRequestWithNullRoles(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Given("Project Admin wants to create a new User with a blank value")
    public void setRequestWithRolesContainBlankValue(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Given("Project Admin wants to create a new User with a null value")
    public void setRequestWithRolesContainNullValue(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
    }

    @Given("an existing User as the following attributes")
    public void createUserForBackground(DocString request) {
        scenarioContext.setContext("createUserRequestBody", request.getContent());
        var apiKey = (String) scenarioContext.getContext("apiKey");
        createUser(apiKey);
    }

    @When("the Project Admin creates a new User with an existing or duplicate key")
    public void createUserWithExistingId() {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        var response = createUser(apiKey);
        scenarioContext.setContext("createUserResponse", response);
    }

    @And("the create user response body includes status {int} and error message {string}")
    public void checkResponseBodyForFailedCreationUser(int status, String errorMessage) {
        var result = (Map) scenarioContext.getContext("createUserResponse");
        assertThat(result.get("status")).isEqualTo(status);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }

    @Given("the Project Admin has the following API key {string} to Test User API")
    public void setApiKey(String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
    }

    @And("a Role to Test User API exists with the following attributes")
    public void createRoleBackground(DocString request) {
        scenarioContext.setContext("requestForCreateRole", request.getContent());
        var apiKey = (String) scenarioContext.getContext("apiKey");
        createRole(apiKey);
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
