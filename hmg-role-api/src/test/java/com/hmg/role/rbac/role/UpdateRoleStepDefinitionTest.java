package com.hmg.role.rbac.role;

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

public class UpdateRoleStepDefinitionTest extends CucumberSpringConfiguration {
    @Given("test")
    public void test() {}

    @Given(
            "the Resource Admin with API key {string} wants to update the Role key {string} with the following attributes:")
    public void setApiKeyAndRoleKeyAndRequestUpdateRole(
            String apiKey, String roleKey, DocString request) {
        scenarioContext.setContext("requestBodyUpdateRole", request.getContent());
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("roleKey", roleKey);
    }

    @When("the Resource Admin updates the Role")
    public void updateRole() throws Exception {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var roleKey = scenarioContext.getContext("roleKey").toString();

        // Parse the raw JSON string into a Map
        String rawJson = scenarioContext.getContext("requestBodyUpdateRole").toString();
        Map<String, Object> requestBody = mapper.readValue(rawJson, HashMap.class);

        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .put("/api/rbac/v1/roles/{roleKey}", roleKey);

        scenarioContext.setContext("responseCodeUpdateRole", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("responseBodyUpdateRole", result);
    }

    @Then("the Role updated successfully with response code {int}")
    public void checkUpdatedRoleResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeUpdateRole"))
                .isEqualTo(expectedResponseCode);
    }

    @And("return the updated Role")
    public void checkUpdatedRoleResponseBody(DocString expectedUpdatedResponse) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedUpdatedResponse.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("responseBodyUpdateRole");

        assertThat(result.get("key")).isEqualTo(expectedResponseMap.get("key"));
        assertThat(result.get("description")).isEqualTo(expectedResponseMap.get("description"));
    }

    @Then("the Role Update fails with response code {int}")
    public void checkResponseCodeForFailedUpdatedRole(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeUpdateRole"))
                .isEqualTo(expectedResponseCode);
    }

    @And("the update role response includes status {int} and error message {string}")
    public void checkResponseBodyForFailedUpdatedRole(int statusCode, String errorMessage) {
        var result = (Map) scenarioContext.getContext("responseBodyUpdateRole");
        assertThat(result.get("status")).isEqualTo(statusCode);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }
}
