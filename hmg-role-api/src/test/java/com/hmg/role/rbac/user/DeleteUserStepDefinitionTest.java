package com.hmg.role.rbac.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public class DeleteUserStepDefinitionTest extends CucumberSpringConfiguration {

    @When("HMG Role Admin wants to delete the User with ID {string} and API key {string}")
    public void setIdAndApiKey(String id, String apiKey) {
        scenarioContext.setContext("id", id);
        scenarioContext.setContext("apiKey", apiKey);
    }

    private void deleteUser() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var id = scenarioContext.getContext("id").toString();
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete("/api/rbac/v1/users/{id}", id);
        scenarioContext.setContext("responseCodeForDeleteUser", response.statusCode());
        if (response.statusCode() != 204) {
            var result = response.thenReturn().as(HashMap.class);
            scenarioContext.setContext("errorResponseForDeleteUser", result);
        }
    }

    @Then("the User deleted successfully with response code {int}")
    public void checkResponseCodeForDeletionUserSuccess(int expectedResponseCode) {
        deleteUser();
        assertThat(scenarioContext.getContext("responseCodeForDeleteUser"))
                .isEqualTo(expectedResponseCode);
    }

    @Then("the User deletion fails with response code {int}")
    public void checkResponseCodeForDeletionUserFailed(int expectedResponseCode) {
        deleteUser();
        assertThat(scenarioContext.getContext("responseCodeForDeleteUser"))
                .isEqualTo(expectedResponseCode);
    }

    @And(
            "the response body for deleting a user, including the status {int} and error message {string}")
    public void checkResponseBodyForDeletionUserFailed(int status, String errorMessage) {
        var result = (Map) scenarioContext.getContext("errorResponseForDeleteUser");
        assertThat(result.get("status")).isEqualTo(status);
        assertThat(result.get("error")).isEqualTo(errorMessage);
    }
}
