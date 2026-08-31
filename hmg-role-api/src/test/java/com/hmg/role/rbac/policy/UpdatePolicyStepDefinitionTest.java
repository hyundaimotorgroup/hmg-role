package com.hmg.role.rbac.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hmg.role.CucumberSpringConfiguration;
import com.hmg.role.admin.member.Member;
import com.hmg.role.admin.member.MemberRepository;
import com.hmg.role.rbac.scope.Scope;
import com.hmg.role.rbac.scope.ScopeRepository;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

public class UpdatePolicyStepDefinitionTest extends CucumberSpringConfiguration {

    @Autowired private ScopeRepository scopeRepository;
    @Autowired private MemberRepository memberRepository;

    @Given(
            "the Resource Admin with API key {string} wants to update the Policy key {string} with the following attributes:")
    public void updatePolicyByFollowingAttributes(
            String apiKey, String key, DocString updateResourceType) {
        scenarioContext.setContext("requestContent", updateResourceType.getContent());
        scenarioContext.setContext("apiKey", apiKey);
        scenarioContext.setContext("key", key);
    }

    @When("the Resource Admin updates the Policy")
    public void whenResourceAdminUpdatesResourceType() {
        updatePolicy();
    }

    private void updatePolicy() {
        var apiKey = scenarioContext.getContext("apiKey").toString();
        var key = scenarioContext.getContext("key").toString();
        var result = createPolicyResponse(apiKey, key);
        scenarioContext.setContext("results", result);
    }

    private Map createPolicyResponse(String apiKey, String key) {
        Response response =
                request()
                        .log()
                        .all()
                        .contentType(ContentType.JSON)
                        .body(scenarioContext.getContext("requestContent"))
                        .headers("X-HMG-ROLE-API-KEY", apiKey)
                        .put("/api/rbac/v1/policies/{key}", key);
        scenarioContext.setContext("responseCode", response.statusCode());
        return response.thenReturn().as(HashMap.class);
    }

    @Then("the Policy updated successfully with response code {int}")
    public void verifyExpectedResponseCode(int expectedResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedResponseCode);
    }

    @And("return the updated Policy")
    public void verifyExpectedResponseBody(DocString expectedUpdatedResponse) throws Exception {
        var expectedResponseMap =
                mapper.readValue(expectedUpdatedResponse.getContent(), HashMap.class);
        var result = (Map) scenarioContext.getContext("results");

        assertThat(result.get("key")).isEqualTo(expectedResponseMap.get("key"));
        assertThat(result.get("description")).isEqualTo(expectedResponseMap.get("description"));
        assertThat(result.get("resourceType")).isEqualTo(expectedResponseMap.get("resourceType"));
        assertThat(result.get("scope")).isEqualTo(expectedResponseMap.get("scope"));
        assertThat(result.get("actions")).isEqualTo(expectedResponseMap.get("actions"));
        assertThat(result.get("roles")).isEqualTo(expectedResponseMap.get("roles"));
        assertThat(result.get("effect")).isEqualTo(expectedResponseMap.get("effect"));
    }

    @Then("the Policy Update fails with response code {int}")
    public void verifyExpectedErrorResponseCode(int expectedErrorResponseCode) {
        assertThat(scenarioContext.getContext("responseCode")).isEqualTo(expectedErrorResponseCode);
    }

    @And("a Scope for API key {string} with specific attributes exists")
    public void createBackgroundScope(String apiKey, DocString jsonReq)
            throws JsonProcessingException {
        var createDtoScopeMap = mapper.readValue(jsonReq.getContent(), HashMap.class);

        Member member = memberRepository.findWithProjectByApiKey(apiKey).orElse(null);
        assert member != null;
        Scope scope =
                Scope.builder()
                        .key(createDtoScopeMap.get("key").toString())
                        .name(createDtoScopeMap.get("name").toString())
                        .project(member.getProject())
                        .build();

        Scope createdScope = scopeRepository.save(scope);

        assertThat(createdScope).isNotNull();
        assertThat(createdScope.getKey()).isEqualTo(createDtoScopeMap.get("scopeKey").toString());
    }
}
