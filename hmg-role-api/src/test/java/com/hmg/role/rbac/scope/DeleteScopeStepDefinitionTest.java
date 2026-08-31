package com.hmg.role.rbac.scope;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

/* Todo */
public class DeleteScopeStepDefinitionTest extends CucumberSpringConfiguration {

    @And("a Scope with the following attributes exists")
    public void aScopeWithTheFollowingAttributesExists(DocString request) {}

    @Given("HMG Role Admin wants to delete a Scope with ID {string}")
    public void hmgRoleAdminWantsToDeleteAScopeWithID(String arg0) {}

    @When("the HMG Role Admin requests to delete the Scope")
    public void theHMGRoleAdminRequestsToDeleteTheScope() {}

    @And("the Scope belongs to a different project")
    public void theScopeBelongsToADifferentProject() {}
}
