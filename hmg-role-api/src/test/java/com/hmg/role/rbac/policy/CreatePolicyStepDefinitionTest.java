package com.hmg.role.rbac.policy;

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

public class CreatePolicyStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("Resource Admin wants to create a new Policy with the following attributes")
    public void createPolicyWithAttributes(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @When("the Resource Admin creates a new Policy")
    public void whenResourceAdminCreatesPolicy() {
        createPolicy();
    }

    @Then("the Policy is created successfully with response code {int}")
    public void verifyPolicyCreatedSuccessfully(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("return the created Policy")
    public void verifyCreatedPolicyResponse(DocString expectedResponseBody) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("results");

        assertThat(result.get("key")).isEqualTo(expectedResponseMap.get("key"));
        assertThat(result.get("description")).isEqualTo(expectedResponseMap.get("description"));
        assertThat(result.get("resourceType")).isEqualTo(expectedResponseMap.get("resourceType"));
        assertThat(result.get("scope")).isEqualTo(expectedResponseMap.get("scope"));
        assertThat(result.get("actions")).isEqualTo(expectedResponseMap.get("actions"));
        assertThat(result.get("roles")).isEqualTo(expectedResponseMap.get("roles"));
        assertThat(result.get("effect")).isEqualTo(expectedResponseMap.get("effect"));
    }

    @Given(
            "an existing Policy from the project A with API key {string} as the following attributes")
    public void createdResourceTypeWithTheFollowingAttributes(String apiKey, DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
        createPolicyWithSameKeyInDifferentProject(apiKey);
    }

    @When(
            "the Resource Admin from the project B with API key {string} wants to Create a new Policy with the same Key")
    public void createResourceTypeInADifferentProject(String apiKey, DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
        createPolicyWithSameKeyInDifferentProject(apiKey);
    }

    @Given("Resource Admin wants to create a new Policy with an empty key")
    public void createPolicyWithAnEmptyKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Then("the Policy creation fails with response code {int}")
    public void verifyResourceTypeCreateFailed(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @Given("Resource Admin wants to create a new Policy with a null key")
    public void createPolicyWithNullKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with a blank key")
    public void createPolicyWithBlankKey(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with an empty resourceType")
    public void createPolicyWithEmptyResourceType(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with a null resourceType")
    public void createPolicyWithNullResourceType(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with an null actions")
    public void createPolicyWithNullActions(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with an empty actions")
    public void createPolicyWithEmptyActions(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given(
            "Resource Admin wants to create a new Policy with an invalid actions containing duplicate values")
    public void createPolicyWithActionsArrayContainsDuplicateValues(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given(
            "Resource Admin wants to create a new Policy with an actions containing a value not associated with the Resource Type")
    public void createPolicyWithActionsContainingValueNotAssociatedWithTheResourceType(
            DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with an null roles")
    public void createPolicyWithNullRoles(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with an empty roles")
    public void createPolicyWithEmptyRoles(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given(
            "Resource Admin wants to create a new Policy with an invalid roles containing duplicate values")
    public void createPolicyWithRolesContainingDuplicateValues(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with an empty effect")
    public void createPolicyWithEmptyEffect(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with a null effect")
    public void createPolicyWithNullEffect(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with a blank effect")
    public void createPolicyWithBlankEffect(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @Given("Resource Admin wants to create a new Policy with invalid effect")
    public void createPolicyWithInvalidEffect(DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    private void createPolicy() {
        var apiKey = scenarioContext.getContext("apiKey");
        var result = createPolicyResponse((String) apiKey);
        scenarioContext.setContext("results", result);
    }

    private void createPolicyWithSameKeyInDifferentProject(String apiKey) {
        var result = createPolicyResponse(apiKey);
        scenarioContext.setContext("results", result);
    }

    private Map createPolicyResponse(String apiKey) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestContent"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .post("/api/rbac/v1/policies");
        scenarioContext.setContext("responseCode", response.statusCode());
        return response.thenReturn().as(HashMap.class);
    }
}
