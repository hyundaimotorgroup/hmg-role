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
import java.util.List;
import java.util.Map;

public class ReadUserStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("The Project Admin wants to get All User with API key {string}")
    public void setApiKey(String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
    }

    @When("the Project Admin requests to get all User")
    public void getAllUser() {
        var apiKey = scenarioContext.getContext("apiKey");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/rbac/v1/users");
        scenarioContext.setContext("responseCodeGetUsers", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("responseBodyGetUsers", result);
    }

    @Then("the request to get all user is successfully with response code {int}")
    public void checkResponseCodeForGetAllUser(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeGetUsers"))
                .isEqualTo(expectedResponseCode);
    }

    @And("a list of all User is returned in the response")
    public void checkResponseBodyForGetAllUser(DocString expectedResponseBody) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var expectedResult = (List<Map>) expectedResponseMap.get("results");
        var response = (Map) scenarioContext.getContext("responseBodyGetUsers");
        var responseResult = (List<Map>) response.get("results");
        var responseMetadata = (Map) response.get("metadata");

        assertThat(responseResult).isNotEmpty();
        assertThat(responseResult).containsAll(expectedResult);
    }

    @And("Project Admin wants to retrieve a User with the ID {string} and API key {string}")
    public void setIdAndApiKey(String id, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("id", id);
    }

    @When("the Project Admin requests to get User by ID")
    public void getUserById() {
        var apiKey = scenarioContext.getContext("apiKey");
        var id = scenarioContext.getContext("id");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/rbac/v1/users/{id}", id);
        scenarioContext.setContext("responseCodeGetUserById", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("responseBodyGetUserByName", result);
    }

    @And("return the User")
    public void checkResponseBodyForGetUserById(DocString expectedResponseBody) throws Exception {
        var expectedResult = mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var responseResult = (Map) scenarioContext.getContext("responseBodyGetUserByName");

        assertThat(responseResult.get("id")).isEqualTo(expectedResult.get("id"));
        assertThat(responseResult.get("name")).isEqualTo(expectedResult.get("name"));
        assertThat(responseResult.get("roles")).isEqualTo(expectedResult.get("roles"));
    }

    @Then("the request to get a user is fails with response code {int}")
    public void checkResponseCodeForGetUserByIdFailed(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeGetUserById"))
                .isEqualTo(expectedResponseCode);
    }

    @Then("the request to get a user is successfully with response code {int}")
    public void checkResponseCodeForGetUserByIdSuccess(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeGetUserById"))
                .isEqualTo(expectedResponseCode);
    }

    @And("the response body for get a role, including the status {int} and error message {string}")
    public void checkResponseBodyForGetUserByIdFailed(int status, String errorMessage) {
        var result = (Map) scenarioContext.getContext("responseBodyGetUserByName");
        assertThat(result.get("status")).isEqualTo(status);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }

    @Given("ad")
    public void theHMGRoleAdminWantsToGetProjectWithTheKeywordSearchNotice() {}
}
