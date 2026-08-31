package com.hmg.role.admin.project;

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

public class ReadProjectStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("a Project with the following attributes exists")
    public void initialProjectExists(DocString request) throws Exception {
        var expectedResponseMap = mapper.readValue(request.getContent(), HashMap.class);
        scenarioContext.setContext("projectKey", expectedResponseMap.get("key"));
        scenarioContext.setContext("projectName", expectedResponseMap.get("name"));
        var apiKey = scenarioContext.getContext("apiKeyProjectAdmin").toString();
        var key = (String) expectedResponseMap.get("key");
        Map result =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(request.getContent())
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .post("/api/admin/v1/projects")
                        .thenReturn()
                        .as(HashMap.class);
    }

    public boolean initialProjectChecking(String apiKey, String key) throws Exception {
        getProjectByKey(key, apiKey);
        if (!scenarioContext.getContext("responseCode").equals(200)) {
            return true;
        }
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete("/api/admin/v1/projects/{projectKey}", key);
        return ((Integer) response.statusCode()).equals(204);
    }

    private void getProjectByKey(String key, String apiKey) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/admin/v1/projects/{projectKey}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }

    @Given("The HMG Role Admin wants to get All Project with API key {string}")
    public void getAllProjectSetup(String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
    }

    @When("the HMG Role Admin requests to get all Project")
    public void getAllProject() {
        var apiKey = scenarioContext.getContext("apiKey");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/admin/v1/projects");
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }

    @And("a list of all Project is returned in the response")
    public void verifyExpectedResult(DocString expectedResponseBody) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var expectedResult = (List<Map>) expectedResponseMap.get("results");
        var expectedMetadata = (Map) expectedResponseMap.get("metadata");
        var response = (Map) scenarioContext.getContext("results");
        var responseResult = (List<Map>) response.get("results");
        var responseMetadata = (Map) response.get("metadata");

        assert !responseResult.isEmpty();
    }

    @Given("HMG Role Admin wants to retrieve a Project with the key {string} and API key {string}")
    public void hmgRoleAdminWantsToGetProjectWithKey(String key, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @When("the HMG Role Admin requests to get Project by key")
    public void getProjectByKey() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        getProjectByKey(key, apiKey);
    }

    @And("return the Project")
    public void returnTheProject(DocString expectedResponseBody) throws Exception {
        var expectedResult = mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var responseResult = (Map) scenarioContext.getContext("results");

        assertThat(responseResult.get("key")).isEqualTo(expectedResult.get("key"));
        assertThat(responseResult.get("name")).isEqualTo(expectedResult.get("name"));
    }

    @Given("The HMG Role Admin wants to get Project with the keyword search {string}")
    public void getProjectByKeywordSearchSetup(String keyword) {
        scenarioContext.setContext("keyword", keyword);
    }

    @When("the HMG Role Admin request to get Project by specific keyword")
    public void getProjectByKeywordSearch() {
        var apiKey = scenarioContext.getContext("apiKey");
        var searchKey = scenarioContext.getContext("keyword");

        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .queryParam("keyword", searchKey)
                        .get("/api/admin/v1/projects");

        scenarioContext.setContext("responseCode", response.statusCode());
        scenarioContext.setContext("results", response.thenReturn().as(HashMap.class));
    }

    @And("a list of all Project is returned empty in the response")
    public void verifyEmptyResult(DocString expectedResponseBody) throws Exception {

        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var expectedResult = (List<Map>) expectedResponseMap.get("results");
        var expectedMetadata = (Map) expectedResponseMap.get("metadata");
        var response = (Map) scenarioContext.getContext("results");
        var responseResult = (List<Map>) response.get("results");
        var responseMetadata = (Map) response.get("metadata");

        assert responseResult.isEmpty();
    }
}
