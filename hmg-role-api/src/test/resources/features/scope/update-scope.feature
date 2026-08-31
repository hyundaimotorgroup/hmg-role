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

  Scenario: Negative - Update Scope with duplicate name
    Given HMG Role Admin wants to create a new Scope with the following attributes
      """json
      {
         "scopeKey": "default_scope"
      }
      """
    And a Scope with name "default_scope" already exists
    When the HMG Role Admin tries to create a new Scope
    Then the response code is 400
    And an error message "Scope name must be unique" is returned

  Scenario: Positive - Create a new Scope with a valid name
    Given HMG Role Admin wants to create a new Scope with the following attributes
      """json
      {
         "scopeKey": "default_scope"
      }
      """
    When the HMG Role Admin creates a new Scope
    Then the Scope is created successfully with response code 201
    And return the created Scope
      """json
      {
         "scopeKey": "default_scope"
      }
      """

  Scenario: Negative - Update Scope with invalid characters
    Given HMG Role Admin wants to update a Scope with Key "default_scope" and the following attributes
      """json
      {
         "scopeKey": "Invalid&Scope*Name"
      }
      """
    When the HMG Role Admin updates the Scope
    Then the response code is 400
    And an error message "Scope name contains invalid characters. Only letters, numbers, '-', and '_' are allowed." is returned

  Scenario: Negative - Update Scope with spaces
    Given HMG Role Admin wants to update a Scope with Key "1" and the following attributes
      """json
      {
         "scopeKey": "Updated Scope Name"
      }
      """
    When the HMG Role Admin updates the Scope
    Then the response code is 400
    And an error message "Scope name cannot contain spaces" is returned

  Scenario: Negative - Update Scope exceeding character limit
    Given HMG Role Admin wants to update a Scope with Key "1" and the following attributes
      """json
      {
         "scopeKey": "this_scope_name_is_way_too_long_123"
      }
      """
    When the HMG Role Admin updates the Scope
    Then the response code is 400
    And an error message "Scope name must be at most 30 characters" is returned
