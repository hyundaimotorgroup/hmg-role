Feature: Create Project

  Background:
    Given the HMG Role Admin has the following API key: '22ee17bd-699e-4b6b-a30b-e854e6c576c5'

  Scenario: Positive: create Project with all required fields.
    Given HMG Role Admin wants to create a new Project with the following attributes
      """json
      {
         "key": "hmg-notice-create",
         "name": "HMG Notice"
      }
      """
    When the HMG Role Admin creates a new Project
    Then the Project is created successfully with response code 201
    And return the created Project
      """json
      {
         "key": "hmg-notice-create",
         "name": "HMG Notice"
      }
      """

  Scenario: Negative: create Project with key: empty
    Given HMG Role Admin wants to create a new Project with an empty key
      """json
      {
         "key": "",
         "name": "HMG Notice"
      }
      """
    When the HMG Role Admin creates a new Project
    Then the Project creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Project with key: null
    Given HMG Role Admin wants to create a new Project with a null key
      """json
      {
         "key": null,
         "name": "HMG Notice"
      }
      """
    When the HMG Role Admin creates a new Project
    Then the Project creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Project with key: blank (whitespace)
    Given HMG Role Admin wants to create a new Project with a blank key
      """json
      {
         "key": "  ",
         "name": "HMG Notice"
      }
      """
    When the HMG Role Admin creates a new Project
    Then the Project creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Project with name: empty
    Given HMG Role Admin wants to create a new Project with an empty name
      """json
      {
         "key": "hmg-notice-create",
         "name": ""
      }
      """
    When the HMG Role Admin creates a new Project
    Then the Project creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Project with name: null
    Given HMG Role Admin wants to create a new Project with a null name
      """json
      {
         "key": "hmg-notice-create",
         "name": null
      }
      """
    When the HMG Role Admin creates a new Project
    Then the Project creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Project with name: blank (whitespace)
    Given HMG Role Admin wants to create a new Project with a blank name
      """json
      {
         "key": "hmg-notice-create",
         "name": "  "
      }
      """
    When the HMG Role Admin creates a new Project
    Then the Project creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Project with an existing/duplicate key
    Given an existing Project as the following attributes
      """json
      {
         "key": "hmg-notice-create",
         "name": "HMG Notice"
      }
      """
    And HMG Role Admin wants to create a new Project with the following attributes
      """json
      {
         "key": "hmg-notice-create",
         "name": "HMG Notice"
      }
      """
    When the HMG Role Admin creates a new Project with an existing or duplicate key
    Then the Project creation fails with response code 409
    And the response body includes status 409 and error message 'Conflict'
