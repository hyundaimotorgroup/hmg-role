Feature: Update Project

  Background:
    Given the HMG Role Admin has the following API key: '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    And a Project with the following attributes exists
      """json
      {
         "key": "hmg-notice",
         "name": "HMG Notice"
      }
      """

  Scenario: Positive: update Project with all required fields.
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Project key 'hmg-notice' with the following attributes:
      """json
      {
         "name": "HMG Notice Update"
      }
      """
    When the HMG Role Admin updates the Project
    Then the Project updated successfully with response code 200
    And return the updated Project
      """json
      {
         "key": "hmg-notice",
         "name": "HMG Notice Update"
      }
      """

  Scenario: Negative: update Project with name: empty
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Project key 'hmg-notice' with the following attributes:
      """json
      {
         "name": ""
      }
      """
    When the HMG Role Admin updates the Project
    Then the Project Update fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: update Project with name: null
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Project key 'hmg-notice' with the following attributes:
      """json
      {
         "name": null
      }
      """
    When the HMG Role Admin updates the Project
    Then the Project Update fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: update Project with name: blank (whitespace)
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Project key 'hmg-notice' with the following attributes:
      """json
      {
         "name": "  "
      }
      """
    When the HMG Role Admin updates the Project
    Then the Project Update fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: update Project by a non-existent Key
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Project key 'Wrong_Key' with the following attributes:
      """json
      {
         "name": "HMG Notice Update"
      }
      """
    When the HMG Role Admin updates the Project
    Then the Project Update fails with response code 404
    And the response body includes status 404 and error message 'Not Found'
