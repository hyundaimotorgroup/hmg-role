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
import org.junit.platform.commons.util.StringUtils;

public class CreateProjectStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("the HMG Role Admin has the following API key: {string}")
    public void setResourceAdminAPIKey(String apiKey) {
        scenarioContext.setContext("apiKeyProjectAdmin", apiKey);
    }

    @Given("HMG Role Admin wants to create a new Project with the following attributes")
    public void hmgRoleAdminWantsToCreateANewProjectWithTheFollowingAttributes(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @When("the HMG Role Admin creates a new Project")
    public void theHmgRoleAdminCreatesProject() throws Exception {
        createProject();
    }

    @Then("the Project is created successfully with response code {int}")
    public void theProjectIsCreatedSuccessfullyWithResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("return the created Project")
    public void returnTheCreatedProject(DocString expectedResponseBody) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("results");

        assertThat(result.get("key")).isEqualTo(expectedResponseMap.get("key"));
        assertThat(result.get("name")).isEqualTo(expectedResponseMap.get("name"));
    }

    @Given("HMG Role Admin wants to create a new Project with an empty key")
    public void hmgRoleAdminWantsToCreateANewProjectWithAnEmptyKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Then("the Project creation fails with response code {int}")
    public void theProjectCreationFailsWithResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @Given("HMG Role Admin wants to create a new Project with a null key")
    public void hmgRoleAdminWantsToCreateANewProjectWithANullKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("HMG Role Admin wants to create a new Project with a blank key")
    public void hmgRoleAdminWantsToCreateANewProjectWithABlankKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("HMG Role Admin wants to create a new Project with an empty name")
    public void hmgRoleAdminWantsToCreateANewProjectWithAnEmptyName(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("HMG Role Admin wants to create a new Project with a null name")
    public void hmgRoleAdminWantsToCreateANewProjectWithANullName(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("HMG Role Admin wants to create a new Project with a blank name")
    public void hmgRoleAdminWantsToCreateANewProjectWithABlankName(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("an existing Project as the following attributes")
    public void anExistingProjectAsTheFollowingAttributes(DocString request) throws Exception {
        scenarioContext.setContext("requestContent", request.getContent());
        var apiKey = (String) scenarioContext.getContext("apiKeyProjectAdmin");
        var content = (Map) mapper.readValue(request.getContent(), HashMap.class);
        var key = (String) content.get("key");
        if (initialProjectChecking(apiKey, key)) {
            createProject();
        }
    }

    @When("the HMG Role Admin creates a new Project with an existing or duplicate key")
    public void theHMGRoleAdminCreatesANewProjectAnExistingDuplicateKey() {
        createProject();
    }

    private void createProject() {
        var apiKey = scenarioContext.getContext("apiKeyProjectAdmin");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestContent"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .post("/api/admin/v1/projects");
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }

    public boolean initialProjectChecking(String apiKey, String key) throws Exception {
        if (StringUtils.isBlank(key)) {
            return true;
        }
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
}
