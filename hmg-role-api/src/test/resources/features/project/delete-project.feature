Feature: Delete Project

  Background:
    Given the HMG Role Admin has the following API key: '22ee17bd-699e-4b6b-a30b-e854e6c576c5'

  Scenario: Positive: delete Project by Key
    And a Project with the following attributes exists
      """json
      {
         "key": "hmg-notice-project-delete",
         "name": "HMG Notice"
      }
      """
    When HMG Role Admin wants to delete the Project with key 'hmg-notice-project-delete' and API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    Then the Project deleted successfully with response code 204

  Scenario: Negative: delete Project by a non-existent Key
    And a Project with the following attributes exists
      """json
      {
         "key": "hmg-notice-project-delete",
         "name": "HMG Notice"
      }
      """
    When HMG Role Admin wants to delete the Project with key 'FailKey' and API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    Then the Project deletion fails with response code 404
    And the response body includes status 404 and error message 'Not Found'
