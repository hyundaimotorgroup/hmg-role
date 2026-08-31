package com.hmg.role.rbac.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadPolicyStepDefinitionTest extends CucumberSpringConfiguration {

    @And("a Policy for API key {string} with specific attributes exists")
    public void initialPolicyExists(String apiKey, DocString request) throws Exception {
        request()
                .log()
                .all()
                .contentType(ContentType.JSON)
                .body(request.getContent())
                .headers("X-HMG-ROLE-API-KEY", apiKey)
                .post("/api/rbac/v1/policies");
    }

    @Given("The Resource Admin wants to get All Policy with API key {string}")
    public void getAllPolicySetup(String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
    }

    @When("the Resource Admin requests to get all Policy")
    public void getAllPolicy() {
        var apiKey = scenarioContext.getContext("apiKey");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/rbac/v1/policies");
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }

    @And("a list of all Policy is returned in the response")
    public void verifyExpectedResult(DocString expectedResponseBody) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var expectedResult = (List<Map>) expectedResponseMap.get("results");
        var expectedMetadata = (Map) expectedResponseMap.get("metadata");
        var response = (Map) scenarioContext.getContext("results");
        var responseResult = (List<Map>) response.get("results");
        var responseMetadata = (Map) response.get("metadata");

        assertThat(responseResult).containsAnyElementsOf(expectedResult);
    }

    @Given("Resource Admin wants to retrieve a Policy with the key {string} and API key {string}")
    public void getPolicyByKeySetup(String key, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @When("the Resource Admin requests to get Policy by key")
    public void getPolicyByKey() {
        var apiKey = scenarioContext.getContext("apiKey");
        var key = scenarioContext.getContext("key");
        getPolicyByKey(key.toString(), apiKey.toString());
    }

    @And("return the Policy")
    public void verifyExpectedResponseBody(DocString expectedResponseBody) throws Exception {
        var expectedResult = mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var responseResult = (Map) scenarioContext.getContext("results");

        assertThat(responseResult.get("key")).isEqualTo(expectedResult.get("key"));
        assertThat(responseResult.get("description")).isEqualTo(expectedResult.get("description"));
        assertThat(responseResult.get("resourceType"))
                .isEqualTo(expectedResult.get("resourceType"));
        assertThat(responseResult.get("scope")).isEqualTo(expectedResult.get("scope"));
        assertThat(responseResult.get("actions")).isEqualTo(expectedResult.get("actions"));
        assertThat(responseResult.get("roles")).isEqualTo(expectedResult.get("roles"));
        assertThat(responseResult.get("effect")).isEqualTo(expectedResult.get("effect"));
    }

    public boolean initialPolicyChecking(String apiKey, String key) throws Exception {
        getPolicyByKey(key, apiKey);
        if (!scenarioContext.getContext("responseCode").equals(200)) {
            return true;
        }
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete("/api/rbac/v1/policies/{key}", key);
        return ((Integer) response.statusCode()).equals(204);
    }

    private void getPolicyByKey(String key, String apiKey) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/rbac/v1/policies/{key}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }
}
