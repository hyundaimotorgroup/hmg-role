Feature: Create Member

  Background:
    Given the HMG Role Admin has the following API key: '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    And a Project with the following attributes exists
      """json
      {
         "key": "hmg-notice-member-create",
         "name": "HMG Notice"
      }
      """

  Scenario: Positive: create Member with all required fields.
    Given HMG Role Admin wants to create a new Member with the following attributes
      """json
      {
         "key": "hmg-notice-admin-1",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57623"
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member is created successfully with response code 201
    And return the created Member
      """json
      {
         "key": "hmg-notice-admin-1",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57623"
      }
      """

  Scenario: Positive: create Member without apiKey
    Given HMG Role Admin wants to create a new Member without apiKey
      """json
      {
         "key": "hmg-notice-admin-2",
         "name": "Shin Tae-yong"
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member is created successfully with response code 201
    And return the created Member with an auto generated UUID
      """json
      {
         "key": "hmg-notice-admin-2",
         "name": "Shin Tae-yong"
      }
      """

  Scenario: Positive: create Member with apiKey: empty
    Given HMG Role Admin wants to create a new Member with an empty apiKey
      """json
      {
         "key": "hmg-notice-admin-3",
         "name": "Shin Tae-yong",
         "apiKey": ""
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member is created successfully with response code 201
    And return the created Member with an auto generated UUID
      """json
      {
         "key": "hmg-notice-admin-3",
         "name": "Shin Tae-yong"
      }
      """

  Scenario: Positive: create Member with apiKey: null
    Given HMG Role Admin wants to create a new Member with a null apiKey
      """json
      {
         "key": "hmg-notice-admin-4",
         "name": "Shin Tae-yong",
         "apiKey": null
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member is created successfully with response code 201
    And return the created Member with an auto generated UUID
      """json
      {
         "key": "hmg-notice-admin-4",
         "name": "Shin Tae-yong"
      }
      """

  Scenario: Positive: create Member with apiKey: blank (whitespace)
    Given HMG Role Admin wants to create a new Member with a blank apiKey
      """json
      {
         "key": "hmg-notice-admin-5",
         "name": "Shin Tae-yong",
         "apiKey": "  "
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member is created successfully with response code 201
    And return the created Member with an auto generated UUID
      """json
      {
         "key": "hmg-notice-admin-5",
         "name": "Shin Tae-yong"
      }
      """

  Scenario: Negative: create Member with key: empty
    Given HMG Role Admin wants to create a new Member with an empty key
      """json
      {
         "key": "",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57601"
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Member with key: null
    Given HMG Role Admin wants to create a new Member with a null key
      """json
      {
         "key": null,
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57601"
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Member with key: blank (whitespace)
    Given HMG Role Admin wants to create a new Member with a blank key
      """json
      {
         "key": "  ",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57641"
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Member with name: empty
    Given HMG Role Admin wants to create a new Member with an empty name
      """json
      {
         "key": "hmg-notice-admin-1",
         "name": "",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57616"
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Member with name: null
    Given HMG Role Admin wants to create a new Member with a null name
      """json
      {
         "key": "hmg-notice-admin-1",
         "name": null,
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57610"
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Member with name: blank (whitespace)
    Given HMG Role Admin wants to create a new Member with a blank name
      """json
      {
         "key": "hmg-notice-admin-1",
         "name": "  ",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57611"
      }
      """
    When the HMG Role Admin creates a new Member
    Then the Member creation fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: create Member with an existing/duplicate key
    Given an existing Member as the following attributes
      """json
      {
         "key": "hmg-notice-admin-1",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57621"
      }
      """
    And HMG Role Admin wants to create a new Member with the following attributes
      """json
      {
         "key": "hmg-notice-admin-1",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57612"
      }
      """
    When the HMG Role Admin creates a new Member with an existing or duplicate key
    Then the Member creation fails with response code 409
    And the response body includes status 409 and error message 'Conflict'

  Scenario: Negative: create Member with an existing/duplicate apiKey
    Given an existing Member as the following attributes
      """json
      {
         "key": "hmg-notice-admin-4",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57631"
      }
      """
    And HMG Role Admin wants to create a new Member with the following attributes
      """json
      {
         "key": "hmg-notice-admin-5",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57631"
      }
      """
    When the HMG Role Admin creates a new Member with an existing or duplicate apiKey
    Then the Member creation fails with response code 409
    And the response body includes status 409 and error message 'Conflict'
