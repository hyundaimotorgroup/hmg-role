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
import java.util.List;
import java.util.Map;

public class ReadRoleStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("The Resource Admin wants to get All Role with API key {string}")
    public void setApiKeyForGetAllRole(String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
    }

    @And("a list of all Role is returned in the response")
    public void checkGetAllResponseBody(DocString expectedResponseBody) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var expectedResult = (List<Map>) expectedResponseMap.get("results");
        var response = (Map) scenarioContext.getContext("responseBodyGetRoles");
        var responseResult = (List<Map>) response.get("results");
        var responseMetadata = (Map) response.get("metadata");

        assertThat(responseResult).isNotEmpty();
        assertThat(responseResult).containsAll(expectedResult);
    }

    @Given("Resource Admin wants to retrieve a Role with the name {string} and API key {string}")
    public void setNameAndApiKey(String name, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("name", name);
    }

    @When("the Resource Admin requests to get Role by name")
    public void getRole() {
        var apiKey = scenarioContext.getContext("apiKey");
        var name = scenarioContext.getContext("name");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/rbac/v1/roles/{name}", name);
        scenarioContext.setContext("responseCodeGetRoleByName", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("responseBodyGetRoleByName", result);
    }

    @And("return the Role")
    public void checkGetRoleResponseBody(DocString expectedResponseBody) throws Exception {
        var expectedResult = mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var responseResult = (Map) scenarioContext.getContext("responseBodyGetRoleByName");

        assertThat(responseResult.get("name")).isEqualTo(expectedResult.get("name"));
        assertThat(responseResult.get("description")).isEqualTo(expectedResult.get("description"));
    }

    @When("the Resource Admin requests to get all Role")
    public void getAllRole() {
        var apiKey = scenarioContext.getContext("apiKey");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/rbac/v1/roles");
        scenarioContext.setContext("responseCodeGetRoles", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("responseBodyGetRoles", result);
    }

    @Then("the request get roles is successfully with response code {int}")
    public void checkGetRolesResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeGetRoles"))
                .isEqualTo(expectedResponseCode);
    }

    @Then("the request get role by name is successfully with response code {int}")
    public void checkGetRoleResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeGetRoleByName"))
                .isEqualTo(expectedResponseCode);
    }

    @Then("the request for get role by name is fails with response code {int}")
    public void checkFailedResponseCodeForGetRole(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeGetRoleByName"))
                .isEqualTo(expectedResponseCode);
    }

    @And("the response body for get role includes status {int} and error message {string}")
    public void checkFailedResponseBodyForGetRole(int status, String errorMessage) {
        var result = (Map) scenarioContext.getContext("responseBodyGetRoleByName");
        assertThat(result.get("status")).isEqualTo(status);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }
}
