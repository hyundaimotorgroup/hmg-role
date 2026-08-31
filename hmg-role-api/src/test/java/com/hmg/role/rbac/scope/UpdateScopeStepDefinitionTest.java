package com.hmg.role.rbac.scope;

import com.hmg.role.CucumberSpringConfiguration;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

/* Todo */
public class UpdateScopeStepDefinitionTest extends CucumberSpringConfiguration {

    @Given("HMG Role Admin wants to update a Scope with Key {string} and the following attributes")
    public void hmgRoleAdminWantsToUpdateAScopeWithIDAndTheFollowingAttributes(
            String key, DocString docString) {}

    @When("the HMG Role Admin updates the Scope")
    public void theHMGRoleAdminUpdatesTheScope() {}
}
