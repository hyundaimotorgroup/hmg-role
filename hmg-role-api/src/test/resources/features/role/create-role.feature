Feature: Create Role

  Background:
    Given The following API keys exist for the Resource Admin:
      | 22ee17bd-699e-4b6b-a30b-e854e6c57552 |

  Scenario: Positive: create Role with all required fields.
    Given Resource Admin wants to create a new Role with the following attributes
      """json
      {
         "name": "ADMIN-1",
         "key": "admin-1-key",
         "description": "Role for the Administrator"
      }
      """
    When the Resource Admin creates a new Role
    Then the Role is created successfully with response code 201
    And return the created Role
      """json
      {
         "name": "ADMIN-1",
         "key": "admin-1-key",
         "description": "Role for the Administrator"
      }
      """

  Scenario: Positive: create Role with description: empty
    Given Resource Admin wants to create a new Role with the following attributes
      """json
      {
         "name": "ADMIN-2",
         "key": "admin-2-key",
         "description": ""
      }
      """
    When the Resource Admin creates a new Role
    Then the Role is created successfully with response code 201
    And return the created Role
      """json
      {
         "name": "ADMIN-2",
         "key": "admin-2-key",
         "description": ""
      }
      """

  Scenario: Positive: create Role with description: blank (whitespace)
    Given Resource Admin wants to create a new Role with the following attributes
      """json
      {
         "name": "ADMIN-3",
         "key": "admin-3-key",
         "description": "  "
      }
      """
    When the Resource Admin creates a new Role
    Then the Role is created successfully with response code 201
    And return the created Role
      """json
      {
         "name": "ADMIN-3",
         "key": "admin-3-key",
         "description": "  "
      }
      """

  Scenario: Positive: create Role with description: null
    Given Resource Admin wants to create a new Role with the following attributes
      """json
      {
         "name": "ADMIN-4",
         "key": "admin-4-key",
         "description": null
      }
      """
    When the Resource Admin creates a new Role
    Then the Role is created successfully with response code 201
    And return the created Role
      """json
      {
         "name": "ADMIN-4",
         "key": "admin-4-key",
         "description": null
      }
      """

  Scenario: Negative: create Role with name: empty
    Given Resource Admin wants to create a new Role with an empty name
      """json
      {
         "name": "",
         "description": "Role for the Administrator"
      }
      """
    When the Resource Admin creates a new Role
    Then the Role creation fails with response code 400
    And the create role response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Role with name: null
    Given Resource Admin wants to create a new Role with a null key
      """json
      {
         "name": null,
         "description": "Role for the Administrator"
      }
      """
    When the Resource Admin creates a new Role
    Then the Role creation fails with response code 400
    And the create role response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Role with name: blank (whitespace)
    Given Resource Admin wants to create a new Role with a null key
      """json
      {
         "name": "  ",
         "description": "Role for the Administrator"
      }
      """
    When the Resource Admin creates a new Role
    Then the Role creation fails with response code 400
    And the create role response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Role with an existing/duplicate name
    Given an existing Role from the project A with API key '22ee17bd-699e-4b6b-a30b-e854e6c57552' as the following attributes
      """json
      {
         "name": "ADMIN-5",
         "key": "admin-5-key",
         "description": "Role for the Administrator"
      }
      """
    When the Resource Admin wants to Create a new Role with API key '22ee17bd-699e-4b6b-a30b-e854e6c57552' as the following attributes
      """json
      {
         "name": "ADMIN-5",
         "key": "admin-5-key",
         "description": "Role for the Administrator"
      }
      """
    Then the Role creation fails with response code 409
    And the create role response body includes status 409 and error message 'Conflict'

  Scenario: Positive: create Role with an existing/duplicate key but with different project
    Given an existing Role from the project A with API key '22ee17bd-699e-4b6b-a30b-e854e6c57552' as the following attributes
      """json
      {
         "name": "ADMIN-6",
         "key": "admin-6-key",
         "description": "Role for the Administrator"
      }
      """
    When the Resource Admin from the project B with API key '22ee17bd-699e-4b6b-a30b-e854e6c57722' wants to Create a new Role with the same name
      """json
      {
         "name": "ADMIN-6",
         "key": "admin-6-key",
         "description": "Role for the Administrator"
      }
      """
    Then the Role is created successfully with response code 201
    And return the created Role
      """json
      {
         "name": "ADMIN-6",
         "key": "admin-6-key",
         "description": "Role for the Administrator"
      }
      """
