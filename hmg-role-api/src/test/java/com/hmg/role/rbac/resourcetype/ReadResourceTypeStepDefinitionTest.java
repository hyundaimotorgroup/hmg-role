package com.hmg.role.rbac.resourcetype;

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

public class ReadResourceTypeStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("The Resource Admin wants to get All Resource Type with API key {string}")
    public void setApiKey(String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
    }

    @When("the Resource Admin requests to get all Resource Type")
    public void getAllResourceTypes() {
        var apiKey = scenarioContext.getContext("apiKey");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/rbac/v1/resource-types");
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }

    @Given(
            "Resource Admin wants to retrieve a Resource Type with the key {string} and API key {string}")
    public void resourceAdminWantsToGetAnResourceTypeByKey(String key, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @Then("the request is successfully with response code {int}")
    public void theRequestIsSuccessfullyWithResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("a list of all Resource Type is returned in the response")
    public void listOfAllResourceTypeIsReturnedInTheResponse(DocString expectedResponseBody)
            throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var expectedResult = (List<Map>) expectedResponseMap.get("results");
        var expectedMetadata = (Map) expectedResponseMap.get("metadata");
        var response = (Map) scenarioContext.getContext("results");
        var responseResult = (List<Map>) response.get("results");
        var responseMetadata = (Map) response.get("metadata");

        assertThat(responseResult).isNotEmpty();
        assertThat(responseResult).containsAnyElementsOf(expectedResult);
    }

    @When("the Resource Admin requests to get Resource Type by key")
    public void theResourceAdminRequestsToGetResourceTypeByKey() {
        var apiKey = scenarioContext.getContext("apiKey");
        var key = scenarioContext.getContext("key");
        getResourceTypeByKey(key.toString(), apiKey.toString());
    }

    private void getResourceTypeByKey(String key, String apiKey) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/rbac/v1/resource-types/{key}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }

    @And("return the resource dataType")
    public void returnTheResourceType(DocString expectedResponseBody) throws Exception {
        var expectedResult = mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var responseResult = (Map) scenarioContext.getContext("results");

        assertThat(responseResult.get("key")).isEqualTo(expectedResult.get("key"));
        assertThat(responseResult.get("description")).isEqualTo(expectedResult.get("description"));
        assertThat(responseResult.get("actions")).isEqualTo(expectedResult.get("actions"));
    }

    @Then("the request is fails with response code {int}")
    public void theRequestIsFailsWithResponseCode(int expectedErrorResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedErrorResponseCode);
    }

    @Then("the response body read includes status {int} and error message {string}")
    public void theErrorResponseReturned(int statusCode, String errorMessage) {
        var result = (Map) scenarioContext.getContext("results");
        assertThat(result.get("status")).isEqualTo(statusCode);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }

    @Given("a Resource Type for API key {string} with specific attributes exists")
    public void createInitialResourceTypeData(String apiKey, DocString request) throws Exception {
        Map result =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(request.getContent())
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .post("/api/rbac/v1/resource-types")
                        .thenReturn()
                        .as(HashMap.class);
    }

    @When(
            "the Resource Admin requests to get all Resource Type with dataType {string} and keyword {string}")
    public void getAllResourceTypesWithTypeAndKeyword(String type, String keyword) {
        var apiKey = scenarioContext.getContext("apiKey");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get(
                                "/api/rbac/v1/resource-types?type={dataType}&keyword={keyword}",
                                type,
                                keyword);
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }

    public boolean initialResourceTypeChecking(String apiKey, String key) throws Exception {
        getResourceTypeByKey(key, apiKey);
        if (!scenarioContext.getContext("responseCode").equals(200)) {
            return true;
        }
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete("/api/rbac/v1/resource-types/{key}", key);
        return ((Integer) response.statusCode()).equals(204);
    }
}
