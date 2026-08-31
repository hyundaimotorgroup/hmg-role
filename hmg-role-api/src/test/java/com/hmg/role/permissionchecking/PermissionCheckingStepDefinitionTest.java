package com.hmg.role.permissionchecking;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionCheckingStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("the Resource Server wants to check permissions with the following attributes:")
    public void theResourceServerWantsToCheckPermissionsWithTheFollowingAttributes(
            DocString request) {
        scenarioContext.setContext("requestContent", request.getContent());
    }

    @When("the Resource Server checks permissions with flattenResponseFormat is {string}")
    public void theResourceServerChecksPermissions(String flattenResponseFormat) throws Exception {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestContent"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .post(
                                "/api/rbac/v1/permissions?flattenResponseFormat="
                                        + flattenResponseFormat);
        scenarioContext.setContext("responseCode", response.statusCode());
        if (response.statusCode() != 200) {
            var result = response.thenReturn().as(HashMap.class);
            scenarioContext.setContext("results", result);
        } else {
            var result = response.thenReturn().as(HashMap.class);
            scenarioContext.setContext("responseResult", result);
        }
    }

    @Then("the permission check is successful with response code {int}")
    public void thePermissionCheckIsSuccessfulWithResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("the response body contains the following attributes:")
    public void theResponseBodyContainsTheFollowingAttributes(DocString expectedResponseBody)
            throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var expectedResult = (List<Map>) expectedResponseMap.get("results");
        var response = (Map) scenarioContext.getContext("responseResult");
        var responseList = (List<Map>) response.get("results");

        expectedResult.stream()
                .forEach(
                        result -> {
                            assertThat(responseList).contains(result);
                        });
    }

    @Then("the permission check fails with response code {int}")
    public void thePermissionCheckFailsWithResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }
}
