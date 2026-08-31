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

  Scenario: Positive - Create a new Scope with a valid name
    Given HMG Role Admin wants to create a new Scope with the following attributes
      """json
      {
         "key": "default_scope",
         "name": "default-scope-key"
      }
      """
    When the HMG Role Admin creates a new Scope
    Then the Scope is created successfully with response code 201
    And return the created Scope
      """json
      {
         "key": "default_scope",
         "name": "default-scope-key"
      }
      """

  Scenario: Negative - Create Scope with duplicate name
    Given HMG Role Admin wants to create a new Scope with the following attributes
      """json
      {
         "key": "default_scope",
         "name": "default-scope-key"
       }
      """
    And a Scope with name "default_scope" already exists
    When the HMG Role Admin tries to create a new Scope
    Then the response code is 400
    And an error message "Scope name must be unique" is returned

  Scenario: Negative - Create Scope without a name
    Given HMG Role Admin wants to create a new Scope with the following attributes
      """json
      {
         "key": "",
         "name": ""
      }
      """
    When the HMG Role Admin tries to create a new Scope
    Then the response code is 400
    And an error message "Scope name is required" is returned

  Scenario: Negative - Create Scope exceeding character limit
    Given HMG Role Admin wants to create a new Scope with the following attributes
      """json
      {
         "key": "this_scope_key_is_way_too_long_123",
         "name": "this_scope_name_is_way_too_long_123"
      }
      """
    When the HMG Role Admin tries to create a new Scope
    Then the response code is 400
    And an error message "Scope name must be at most 30 characters" is returned

  Scenario: Negative - Create Scope with invalid characters
    Given HMG Role Admin wants to create a new Scope with the following attributes
      """json
      {
         "key": "Invalid@Scope#123",
         "name": "Invalid@Scope#123"
      }
      """
    When the HMG Role Admin tries to create a new Scope
    Then the response code is 400
    And an error message "Scope name contains invalid characters. Only letters, numbers, '-', and '_' are allowed." is returned

  Scenario: Negative - Create Scope with spaces
    Given HMG Role Admin wants to create a new Scope with the following attributes
      """json
      {
         "key": "Scope Name With Spaces",
         "name": "scopeNameWithSpaces"
      }
      """
    When the HMG Role Admin tries to create a new Scope
    Then the response code is 400
    And an error message "Scope name cannot contain spaces" is returned
