package com.hmg.role.rbac.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;

public class DeletePolicyStepDefinitionTest extends CucumberSpringConfiguration {
    @When("Resource Admin wants to delete the Policy with key {string} and API key {string}")
    public void deletePolicyByKey(String key, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @Then("the Policy deleted successfully with response code {int}")
    public void verifyResponseCodeDeleted(int expectedResponseCode) {
        deletePolicy();
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @Then("the Policy deletion fails with response code {int}")
    public void verifyDeletionFails(int expectedResponseCode) {
        deletePolicy();
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    private void deletePolicy() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete("/api/rbac/v1/policies/{key}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
        if (response.statusCode() != 204) {
            var result = response.thenReturn().as(HashMap.class);
            scenarioContext.setContext("results", result);
        }
    }
}
