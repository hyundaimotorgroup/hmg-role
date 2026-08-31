package com.hmg.role.admin.member;

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

public class UpdateMemberStepDefinitionTest extends CucumberSpringConfiguration {

    @Given(
            "the HMG Role Admin with API key {string} wants to update the Member key {string} with the following attributes:")
    public void updateMemberKeyWithTheFollowingAttributes(
            String apiKey, String key, DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @When("the HMG Role Admin updates the Member")
    public void theHMGRoleAdminUpdatesTheMember() {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        var projectKey = (String) scenarioContext.getContext("projectKey");
        var key = (String) scenarioContext.getContext("key");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestContent"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .put(
                                "/api/admin/v1/projects/{projectKey}/members/{memberKey}",
                                projectKey,
                                key);
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }

    @Then("the Member updated successfully with response code {int}")
    public void theMemberUpdatedSuccessfullyWithResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("return the updated Member")
    public void returnTheUpdatedMember(DocString expectedUpdatedResponse) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedUpdatedResponse.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("results");

        assertThat(result.get("key")).isEqualTo(expectedResponseMap.get("key"));
        assertThat(result.get("name")).isEqualTo(expectedResponseMap.get("name"));
        assertThat(result.get("apiKey")).isEqualTo(expectedResponseMap.get("apiKey"));
    }

    @Then("the Member Update fails with response code {int}")
    public void theMemberUpdateFailsWithResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }
}
