Feature: Delete Member

  Background:
    Given the HMG Role Admin has the following API key: '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    And a Project with the following attributes exists
      """json
      {
         "key": "hmg-notice-member-delete",
         "name": "HMG Notice"
      }
      """
    And an existing Member as the following attributes
      """json
        {
           "key": "hmg-notice-admin-del",
           "name": "Shin Tae-yong",
           "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57691"
        }
      """

  Scenario: Positive: delete Member by Key
    When HMG Role Admin wants to delete the Member with key 'hmg-notice-admin-del' and API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    Then the Member deleted successfully with response code 204

  Scenario: Negative: delete Member by a non-existent Key
    When HMG Role Admin wants to delete the Member with key 'FailKey' and API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    Then the Member deletion fails with response code 404
    And the response body includes status 404 and error message 'Not Found'
