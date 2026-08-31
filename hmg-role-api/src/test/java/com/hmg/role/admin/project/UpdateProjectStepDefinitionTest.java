package com.hmg.role.admin.project;

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

public class UpdateProjectStepDefinitionTest extends CucumberSpringConfiguration {

    @Given(
            "the HMG Role Admin with API key {string} wants to update the Project key {string} with the following attributes:")
    public void updateProjectWithTheFollowingAttributes(
            String apiKey, String key, DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @When("the HMG Role Admin updates the Project")
    public void whenHmgRoleAdminUpdatesProject() {
        updateProject();
    }

    private void updateProject() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        var result = createProjectResponse(apiKey, key);
        scenarioContext.setContext("results", result);
    }

    private Map createProjectResponse(String apiKey, String key) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestContent"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .put("/api/admin/v1/projects/{projectKey}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
        return response.thenReturn().as(HashMap.class);
    }

    @Then("the Project updated successfully with response code {int}")
    public void theProjectUpdatedSuccessfullyWithResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("return the updated Project")
    public void returnTheUpdatedProject(DocString expectedUpdatedResponse) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedUpdatedResponse.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("results");

        assertThat(result.get("key")).isEqualTo(expectedResponseMap.get("key"));
        assertThat(result.get("name")).isEqualTo(expectedResponseMap.get("name"));
    }

    @Then("the Project Update fails with response code {int}")
    public void theProjectUpdateFailsWithResponseCode(int expectedErrorResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedErrorResponseCode);
    }
}
