package com.hmg.role.rbac.scope;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

/* Todo */
public class ReadScopeStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("HMG Role Admin wants to retrieve a Scope with Key {string}")
    public void hmgRoleAdminWantsToRetrieveAScopeWithID(String arg0) {}

    @When("the HMG Role Admin requests the Scope details")
    public void theHMGRoleAdminRequestsTheScopeDetails() {}

    @And("return the Scope details")
    public void returnTheScopeDetails(DocString docString) {}
}
