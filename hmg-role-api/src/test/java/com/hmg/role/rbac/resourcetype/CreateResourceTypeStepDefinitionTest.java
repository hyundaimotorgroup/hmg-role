package com.hmg.role.rbac.resourcetype;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.platform.commons.util.StringUtils;

public class CreateResourceTypeStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("The following API keys exist for the Resource Admin:")
    public void theFollowingProjectsAndResourceTypesExist(DataTable dataTable) {
        scenarioContext.setContext("apiKey", dataTable.asList(String.class).getFirst());
    }

    @Given("Resource Admin wants to create a new Resource Type with the following attributes")
    public void givenResourceTypeAttributes(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @When("the Resource Admin creates a new Resource Type")
    public void whenResourceAdminCreatesResourceType() throws Exception {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        var content =
                (Map)
                        mapper.readValue(
                                (String) scenarioContext.getContext("requestContent"),
                                HashMap.class);
        var key = (String) content.get("key");
        if (initialResourceTypeChecking(apiKey, key)) {
            createResourceType();
        }
    }

    public boolean initialResourceTypeChecking(String apiKey, String key) throws Exception {
        if (StringUtils.isBlank(key)) {
            return true;
        }
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

    @Then("the Resource Type is created successfully with response code {int}")
    public void verifyResourceTypeCreatedSuccessfully(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("return the created resource dataType")
    public void verifyCreatedResourceTypeResponseBody(DocString expectedResponseBody)
            throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("results");

        assertThat(result.get("key")).isEqualTo(expectedResponseMap.get("key"));
        assertThat(result.get("description")).isEqualTo(expectedResponseMap.get("description"));
        assertThat(result.get("actions")).isEqualTo(expectedResponseMap.get("actions"));
    }

    @Given("Resource Admin wants to create a new Resource Type with an empty key")
    public void givenCreateWithEmptyKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @And("Resource Admin wants to create a new Resource Type with a null key")
    public void createWithNullKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @And("Resource Admin wants to create a new Resource Type with a blank key")
    public void createWithBlankKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Then("the Resource Type creation fails with response code {int}")
    public void verifyResourceTypeCreateFailed(int expectedErrorStatusCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedErrorStatusCode);
    }

    @Given("Resource Admin wants to create a new Resource Type with an empty name")
    public void givenCreateWithEmptyName(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Resource Type with an null name")
    public void givenCreateWithNullName(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Resource Type with an empty actions")
    public void givenCreateWithEmptyActions(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @And("Resource Admin wants to create a new Resource Type with an null actions")
    public void createWithNullActions(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create Multiple Resource Type with the following attributes")
    public void givenResourceTypesAttributes(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @When("The Resource Admin creates Multiple Resource Type")
    public void whenResourceAdminCreatedResourceTypes() {
        createMultipleResourceType();
    }

    @Then("The Resource Type create succeeded with response code {int}")
    public void verifyResourceTypesCreatedSuccessfully(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("return the created multiple resource dataType")
    public void verifyCreatedResourceTypesResponseBody(DocString expectedResponseBody)
            throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);

        var expectedResponsesMap = (List<Map>) expectedResponseMap.get("results");

        var response = (Response) scenarioContext.getContext("multipleResponse");
        var results = response.thenReturn().as(HashMap.class);

        var resultsList = (List<Map>) results.get("results");

        for (int i = 0; i < expectedResponsesMap.size(); i++) {
            assertThat(resultsList.get(i).get("key"))
                    .isEqualTo(expectedResponsesMap.get(i).get("key"));
            assertThat(resultsList.get(i).get("description"))
                    .isEqualTo(expectedResponsesMap.get(i).get("description"));
            assertThat(resultsList.get(i).get("actions"))
                    .isEqualTo(expectedResponsesMap.get(i).get("actions"));
        }
    }

    @Given("Resource Admin wants to create Multiple Resource Type with an empty request body")
    public void givenCreateResourceTypesWithEmptyRequest(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given(
            "the Resource Admin wants to create Multiple Resource Type with more than 100 resource dataType data")
    public void givenCreateResourceTypesWithToManyData(DocString request) throws Exception {
        var initialRequestData = mapper.readValue(request.getContent(), HashMap[].class);
        List<Map<String, Object>> requestBodyList = new ArrayList<>();
        for (int i = 80; i <= 190; i++) {
            Map<String, Object> modifiedResourceType =
                    Map.of(
                            "key",
                            "FAQ-" + i,
                            "description",
                            initialRequestData[0].get("description") + " " + i,
                            "actions",
                            initialRequestData[0].get("actions"));
            requestBodyList.add(modifiedResourceType);
        }
        scenarioContext.setContext("requestContent", requestBodyList);
    }

    @Given("Resource Admin wants to create Multiple Resource Type with Duplicate Key")
    public void givenCreateResourceTypesWithDuplicateKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @And("verify whether data with key {string} has been rolled back")
    public void verifyWhetherDataWithKeyHasBeenRolledBack(String key) {
        assert isDataRolledBack(key);
    }

    @And("the response body includes status {int} and error message {string}")
    public void verifyErrorResponseDetails(int statusCode, String errorMessage) {
        var result = (Map) scenarioContext.getContext("results");
        assertThat(result.get("status")).isEqualTo(statusCode);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }

    @And("set content")
    public void setContent(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given(
            "an existing Resource Type from the project A with API key {string} as the following attributes")
    public void createdResourceTypeWithTheFollowingAttributes(String apiKey, DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
        createResourceTypeWithSameKeyInDifferentProject(apiKey);
    }

    @And(
            "the Resource Admin from the project B with API key {string} wants to Create a new Resource Type with the same Key")
    public void createResourceTypeInADifferentProject(String apiKey, DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
        scenarioContext.setContext("apiKeyForDuplicateTest", apiKey);
    }

    private void createResourceType() {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        var result = createResourceTypeResponse(apiKey);
        scenarioContext.setContext("results", result);
    }

    private void createResourceTypeWithSameKeyInDifferentProject(String apiKey) {
        var result = createResourceTypeResponse(apiKey);
        scenarioContext.setContext("results", result);
    }

    private Map createResourceTypeResponse(String apiKey) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestContent"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .post("/api/rbac/v1/resource-types");
        scenarioContext.setContext("responseCode", response.statusCode());
        return response.thenReturn().as(HashMap.class);
    }

    private void createMultipleResourceType() {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestContent"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .post("/api/rbac/v1/resource-types?multiple=true");
        scenarioContext.setContext("responseCode", response.statusCode());
        scenarioContext.setContext("multipleResponse", response);
    }

    private boolean isDataRolledBack(String key) {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        var response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/rbac/v1/resource-types/{key}", key);
        return response.statusCode() != 200;
    }

    @When("the Resource Admin creates a new Resource Type with an existing or duplicate key")
    public void theResourceAdminCreatesANewResourceTypeWithAnExistingOrDuplicateKey() {
        var apiKey = (String) scenarioContext.getContext("apiKeyForDuplicateTest");
        createResourceTypeWithSameKeyInDifferentProject(apiKey);
    }

    @And("the error response body includes status {int} and error message {string}")
    public void theErrorResponseBodyIncludesStatusAndErrorMessageBadRequest(
            int expectedStatusCode, String expectedErrorMessage) {
        var response = (Response) scenarioContext.getContext("multipleResponse");
        var result = response.thenReturn().as(HashMap.class);

        assertThat(result.get("status")).isEqualTo(expectedStatusCode);
        assertThat(result.get("error")).isEqualTo(expectedErrorMessage);
    }
}
