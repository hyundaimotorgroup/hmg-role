package com.hmg.role.admin.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.hmg.role.CucumberSpringConfiguration;
import com.hmg.role.util.dto.PageRequestDto;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadMemberStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("The HMG Role Admin wants to get All Member with API key {string}")
    public void theHMGRoleAdminWantsToGetAllMemberWithAPIKey(String apiKey) {
        scenarioContext.setContext("apiKeyProjectAdmin", apiKey);
    }

    @When("the HMG Role Admin requests to get all Member")
    public void theHMGRoleAdminRequestsToGetAllMember() {
        var apiKey = scenarioContext.getContext("apiKeyProjectAdmin");
        var projectKey = scenarioContext.getContext("projectKey");
        PageRequestDto requestDto = new PageRequestDto();
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get("/api/admin/v1/projects/{projectKey}/members", projectKey);
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }

    @And("a list of all Member is returned in the response")
    public void aListOfAllMemberIsReturnedInTheResponse(DocString expectedResponseBody)
            throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var expectedResult = (List<Map>) expectedResponseMap.get("results");
        var expectedMetadata = (Map) expectedResponseMap.get("metadata");
        var response = (Map) scenarioContext.getContext("results");
        var responseResult = (List<Map>) response.get("results");
        var responseMetadata = (Map) response.get("metadata");

        assertThat(responseResult.getFirst().get("key"))
                .isEqualTo(expectedResult.getFirst().get("key"));
        assertThat(responseResult.getFirst().get("name"))
                .isEqualTo(expectedResult.getFirst().get("name"));
        assertThat(responseResult.getFirst().get("apiKey"))
                .isEqualTo(expectedResult.getFirst().get("apiKey"));
        assertThat(responseMetadata.get("totalCount"))
                .isEqualTo(expectedMetadata.get("totalCount"));
        assertThat(responseMetadata.get("totalPageCount"))
                .isEqualTo(expectedMetadata.get("totalPageCount"));
        assertThat(responseMetadata.get("size")).isEqualTo(expectedMetadata.get("size"));
    }

    @Given("HMG Role Admin wants to retrieve a Member with the key {string} and API key {string}")
    public void hmgRoleAdminWantsToRetrieveAMemberWithMemberKeyAndAPIKey(
            String key, String apiKey) {
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @When("the HMG Role Admin requests to get Member by key")
    public void theHMGRoleAdminRequestsToGetMemberByKey() {
        var apiKey = (String) scenarioContext.getContext("apiKey");
        var key = (String) scenarioContext.getContext("key");
        var projectKey = (String) scenarioContext.getContext("projectKey");
        getMemberByKey(key, apiKey, projectKey);
    }

    @And("return the Member")
    public void returnTheMember(DocString expectedResponseBody) throws Exception {
        var expectedResult = mapper.readValue(expectedResponseBody.getContent(), HashMap.class);
        var responseResult = (Map) scenarioContext.getContext("results");

        assertThat(responseResult.get("key")).isEqualTo(expectedResult.get("key"));
        assertThat(responseResult.get("name")).isEqualTo(expectedResult.get("name"));
        assertThat(responseResult.get("apiKey")).isEqualTo(expectedResult.get("apiKey"));
    }

    private void getMemberByKey(String key, String apiKey, String projectKey) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .get(
                                "/api/admin/v1/projects/{projectKey}/members/{memberKey}",
                                projectKey,
                                key);
        scenarioContext.setContext("responseCode", response.statusCode());
        var result = response.thenReturn().as(HashMap.class);
        scenarioContext.setContext("results", result);
    }
}
