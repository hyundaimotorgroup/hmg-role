package com.hmg.role.rbac.scope;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/* Todo */
public class CreateScopeStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("HMG Role Admin wants to create a new Scope with the following attributes")
    public void hmgRoleAdminWantsToCreateANewScopeWithTheFollowingAttributes(DocString request) {}

    @When("the HMG Role Admin creates a new Scope")
    public void theHMGRoleAdminCreatesANewScope() {}

    @Then("the Scope is created successfully with response code {int}")
    public void theScopeIsCreatedSuccessfullyWithResponseCode(int arg0) {}

    @And("return the created Scope")
    public void returnTheCreatedScope(DocString request) {}

    @And("a Scope with name {string} already exists")
    public void aScopeWithNameAlreadyExists(String arg0) {}

    @When("the HMG Role Admin tries to create a new Scope")
    public void theHMGRoleAdminTriesToCreateANewScope() {}

    @Then("the response code is {int}")
    public void theResponseCodeIs(int arg0) {}

    @And("an error message {string} is returned")
    public void anErrorMessageIsReturned(String arg0) {}
}
