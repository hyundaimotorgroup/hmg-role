Feature: Update Member

  Background:
    Given the HMG Role Admin has the following API key: '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    And a Project with the following attributes exists
      """json
      {
         "key": "hmg-notice-member-update",
         "name": "HMG Notice"
      }
      """
    And an existing Member as the following attributes
      """json
        {
           "key": "hmg-notice-admin-update",
           "name": "Shin Tae-yong",
           "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c576c7"
        }
      """

  Scenario: Positive: update Member with all required fields.
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Member key 'hmg-notice-admin-update' with the following attributes:
      """json
      {
         "name": "Name Update",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c576c9"
      }
      """
    When the HMG Role Admin updates the Member
    Then the Member updated successfully with response code 200
    And return the updated Member
      """json
      {
         "key": "hmg-notice-admin-update",
         "name": "Name Update",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c576c9"
      }
      """

  Scenario: Negative: update Member with name: empty
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Member key 'hmg-notice-admin-update' with the following attributes:
      """json
      {
         "name": "",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c576c522"
      }
      """
    When the HMG Role Admin updates the Member
    Then the Member Update fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: update Member with name: null
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Member key 'hmg-notice-admin-update' with the following attributes:
      """json
      {
         "name": null,
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c576c523"
      }
      """
    When the HMG Role Admin updates the Member
    Then the Member Update fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: update Member with name: blank (whitespace)
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Member key 'hmg-notice-admin-update' with the following attributes:
      """json
      {
         "name": "  ",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c576c532"
      }
      """
    When the HMG Role Admin updates the Member
    Then the Member Update fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: update Member with apiKey: empty
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Member key 'hmg-notice-admin-update' with the following attributes:
      """json
      {
         "name": "Shin Tae-yong",
         "apiKey": ""
      }
      """
    When the HMG Role Admin updates the Member
    Then the Member Update fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: update Member with apiKey: null
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Member key 'hmg-notice-admin-update' with the following attributes:
      """json
      {
         "name": "Shin Tae-yong",
         "apiKey": null
      }
      """
    When the HMG Role Admin updates the Member
    Then the Member Update fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: update Member with apiKey: blank (whitespace)
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Member key 'hmg-notice-admin-update' with the following attributes:
      """json
      {
         "name": "Shin Tae-yong",
         "apiKey": "  "
      }
      """
    When the HMG Role Admin updates the Member
    Then the Member Update fails with response code 400
    And the response body includes status 400 and error message 'Bad Request'

  Scenario: Negative: Update Member by a non-existent Key
    Given the HMG Role Admin with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5' wants to update the Member key 'hmg-notice-fail' with the following attributes:
      """json
      {
         "name": "Name Update",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57622"
      }
      """
    When the HMG Role Admin updates the Member
    Then the Member Update fails with response code 404
    And the response body includes status 404 and error message 'Not Found'
