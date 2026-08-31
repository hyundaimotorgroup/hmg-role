package com.hmg.role.admin.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;

public class DeleteMemberStepDefinitionTest extends CucumberSpringConfiguration {
    @When("HMG Role Admin wants to delete the Member with key {string} and API key {string}")
    public void hmgRoleAdminWantsToDeleteTheMemberWithMemberKeyAndAPIKey(
            String key, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @Then("the Member deleted successfully with response code {int}")
    public void theMemberDeletedSuccessfullyWithResponseCode(int expectedResponseCode) {
        deleteMember();
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @Then("the Member deletion fails with response code {int}")
    public void theMemberDeletionFailsWithResponseCode(int expectedResponseCode) {
        deleteMember();
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    private void deleteMember() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        var projectKey = scenarioContext.getContext("projectKey").toString();
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .delete(
                                "/api/admin/v1/projects/{projectKey}/members/{memberKey}",
                                projectKey,
                                key);
        scenarioContext.setContext("responseCode", response.statusCode());
        if (response.statusCode() != 204) {
            var result = response.thenReturn().as(HashMap.class);
            scenarioContext.setContext("results", result);
        }
    }
}
