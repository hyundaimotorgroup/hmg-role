package com.hmg.role.admin.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;

public class DeleteProjectStepDefinitionTest extends CucumberSpringConfiguration {

    @When("HMG Role Admin wants to delete the Project with key {string} and API key {string}")
    public void resourceAdminWantsToDeleteResourceTypeByKey(String key, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @Then("the Project deleted successfully with response code {int}")
    public void thenDelete(int expectedResponseCode) {
        deleteProject();
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @Then("the Project deletion fails with response code {int}")
    public void theResourceTypeWasFailedToDelete(int expectedResponseCode) {
        deleteProject();
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    private void deleteProject() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete("/api/admin/v1/projects/{projectKey}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
        if (response.statusCode() != 204) {
            var result = response.thenReturn().as(HashMap.class);
            scenarioContext.setContext("results", result);
        }
    }
}
