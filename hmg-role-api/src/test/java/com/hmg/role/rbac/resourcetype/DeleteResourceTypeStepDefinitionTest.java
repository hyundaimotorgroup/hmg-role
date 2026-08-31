package com.hmg.role.rbac.resourcetype;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;

public class DeleteResourceTypeStepDefinitionTest extends CucumberSpringConfiguration {

    @When("Resource Admin wants to delete the Resource Type with key {string} and API key {string}")
    public void resourceAdminWantsToDeleteResourceTypeByKey(String key, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @Then("the Resource Type deleted successfully with response code {int}")
    public void thenDelete(int responseCode) {
        deleteResourceType();
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(responseCode);
    }

    private void deleteResourceType() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete("/api/rbac/v1/resource-types/{key}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
    }

    @Then("the Resource Type deletion fails with response code {int}")
    public void theResourceTypeWasFailedToDelete(int expectedErrorResponseCode) {
        deleteResourceTypeFails();
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedErrorResponseCode);
    }

    private void deleteResourceTypeFails() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete("/api/rbac/v1/resource-types/{key}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }
}
