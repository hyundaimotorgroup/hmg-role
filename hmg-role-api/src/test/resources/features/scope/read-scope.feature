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

  Scenario: Negative - Retrieve Scope by Key when Scope does not exist
    Given HMG Role Admin wants to retrieve a Scope with Key "999"
    When the HMG Role Admin requests the Scope details
    Then the response code is 404
    And an error message "Scope not found" is returned

  Scenario: Positive - Retrieve existing Scope by Key
    Given HMG Role Admin wants to retrieve a Scope with Key "default_scope"
    When the HMG Role Admin requests the Scope details
    Then the response code is 200
    And return the Scope details
      """json
      {
         "scopeKey": "default_scope"
      }
      """
