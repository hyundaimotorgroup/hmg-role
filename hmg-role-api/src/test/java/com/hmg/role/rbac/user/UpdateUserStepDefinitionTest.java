package com.hmg.role.rbac.user;

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

public class UpdateUserStepDefinitionTest extends CucumberSpringConfiguration {
    @Given("Update User Test")
    public void UpdateUserTest() {}

    @Given(
            "the Project Admin with API key {string} wants to update the User ID {string} with the following attributes:")
    public void setApiKeyAndIdAndRequestForUpdateUser(String apiKey, String id, DocString request) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("id", id);
        scenarioContext.setContext("requestBodyUpdateUser", request.getContent());
    }

    @When("the Project Admin updates the User")
    public void updateUser() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var id = scenarioContext.getContext("id").toString();
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestBodyUpdateUser"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .put("/api/rbac/v1/users/{id}", id);
        scenarioContext.setContext("responseCodeUpdateUser", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("responseBodyUpdateUser", result);
    }

    @Then("the User updated successfully with response code {int}")
    public void checkResponseCodeForUpdateUserSuccess(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeUpdateUser"))
                .isEqualTo(expectedResponseCode);
    }

    @And("return the updated User")
    public void checkResponseBodyForUpdateUserSuccess(DocString expectedResponseBody)
            throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("responseBodyUpdateUser");

        assertThat(result.get("id")).isEqualTo(expectedResponseMap.get("id"));
        assertThat(result.get("name")).isEqualTo(expectedResponseMap.get("name"));
        assertThat(result.get("roles")).isEqualTo(expectedResponseMap.get("roles"));
    }

    @Then("the User Update fails with response code {int}")
    public void checkResponseCodeForUpdateUserFailed(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCodeUpdateUser"))
                .isEqualTo(expectedResponseCode);
    }

    @And("the response body for update a user, including status {int} and error message {string}")
    public void checkResponseBodyForUpdateUserFailed(int status, String errorMessage) {
        var result = (Map) scenarioContext.getContext("responseBodyUpdateUser");
        assertThat(result.get("status")).isEqualTo(status);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }
}
