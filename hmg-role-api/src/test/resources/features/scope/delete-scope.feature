Feature: Scope Management

  Background:
    Given the HMG Role Admin has the following API key: '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    And a Project with the following attributes exists
      """json
      {
         "key": "hmg-notice-scope",
         "name": "HMG Notice"
      }
      """
    And a Scope with the following attributes exists
      """json
      {
         "name": "default_scope"
      }
      """

  Scenario: Positive - Delete Scope by ID
    Given HMG Role Admin wants to delete a Scope with ID "1"
    When the HMG Role Admin requests to delete the Scope
    Then the response code is 204

  Scenario: Negative - Delete Scope by ID when Scope does not exist
    Given HMG Role Admin wants to delete a Scope with ID "999"
    When the HMG Role Admin requests to delete the Scope
    Then the response code is 404
    And an error message "Scope not found" is returned

  Scenario: Negative - Delete Scope with ID that belongs to another project
    Given HMG Role Admin wants to delete a Scope with ID "5"
    And the Scope belongs to a different project
    When the HMG Role Admin requests to delete the Scope
    Then the response code is 403
    And an error message "You do not have permission to delete this Scope" is returned

  Scenario: Positive - Delete Scope by ID
    Given HMG Role Admin wants to delete a Scope with ID "1"
    When the HMG Role Admin requests to delete the Scope
    Then the response code is 204
