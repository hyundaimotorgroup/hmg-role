Feature: Read Member

  Background:
    Given the HMG Role Admin has the following API key: '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    And a Project with the following attributes exists
      """json
      {
         "key": "hmg-notice-member",
         "name": "HMG Notice"
      }
      """

  Scenario: Positive: Get Members
    Given The HMG Role Admin wants to get All Member with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    And an existing Member as the following attributes
      """json
      {
         "key": "hmg-notice-admin-read",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57625"
      }
      """
    When the HMG Role Admin requests to get all Member
    Then the request is successfully with response code 200
    And a list of all Member is returned in the response
      """json
      {
         "results": [
            {
               "key": "hmg-notice-admin-read",
               "name": "Shin Tae-yong",
               "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57625"
            }
         ],
         "metadata": {
            "totalCount": 1,
            "totalPageCount": 1,
            "size": 10
         }
      }
      """

  Scenario: Positive: Get a Member by key
    Given an existing Member as the following attributes
      """json
      {
         "key": "hmg-notice-admin-read-2",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57683"
      }
      """
    And HMG Role Admin wants to retrieve a Member with the key 'hmg-notice-admin-read-2' and API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    When the HMG Role Admin requests to get Member by key
    Then the request is successfully with response code 200
    And return the Member
      """json
      {
         "key": "hmg-notice-admin-read-2",
         "name": "Shin Tae-yong",
         "apiKey": "22ee17bd-699e-4b6b-a30b-e854e6c57683"
      }
      """

  Scenario: Negative: Get a Member by a non-existent Key
    Given HMG Role Admin wants to retrieve a Member with the key 'wrong_key' and API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    When the HMG Role Admin requests to get Member by key
    Then the request is fails with response code 404
    And the response body read includes status 404 and error message 'Not Found'
