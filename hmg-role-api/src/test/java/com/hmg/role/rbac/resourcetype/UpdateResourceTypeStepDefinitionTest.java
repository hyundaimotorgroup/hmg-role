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
import java.util.Map;

public class UpdateResourceTypeStepDefinitionTest extends CucumberSpringConfiguration {

    @Given(
            "the Resource Admin with API key {string} wants to update the Resource Type {string} with the following attributes:")
    public void givenResourceTypeAttributes(
            String apiKey, String key, DocString updateResourceType) {
        scenarioContext.setContext("requestContent", updateResourceType.getContent());
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @When("the Resource Admin updates the Resource Type")
    public void whenResourceAdminUpdatesResourceType() {
        updateResourceType();
    }

    private void updateResourceType() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        var result = createResourceTypeResponse(apiKey, key);
        scenarioContext.setContext("results", result);
    }

    private Map createResourceTypeResponse(String apiKey, String key) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestContent"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .put("/api/rbac/v1/resource-types/{key}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
        return response.thenReturn().as(HashMap.class);
    }

    @Then("the Resource Type updated successfully with response code {int}")
    public void verifyResourceTypeUpdatedSuccessfully(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("return the updated resource dataType")
    public void verifyUpdatedResourceTypeResponseBody(DocString expectedUpdatedResponse)
            throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedUpdatedResponse.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("results");

        assertThat(result.get("key")).isEqualTo(expectedResponseMap.get("key"));
        assertThat(result.get("description")).isEqualTo(expectedResponseMap.get("description"));
        assertThat(result.get("actions")).isEqualTo(expectedResponseMap.get("actions"));
    }

    @Then("the Resource Type update fails with response code {int}")
    public void verifyResourceTypeUpdateFailed(int expectedErrorResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedErrorResponseCode);
    }
}
