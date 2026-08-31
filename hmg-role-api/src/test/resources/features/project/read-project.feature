Feature: Read Project

  Background:
    Given the HMG Role Admin has the following API key: '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    And a Project with the following attributes exists
      """json
      {
         "key": "hmg-notice-read",
         "name": "HMG Notice"
      }
      """

  Scenario: Positive: Get Projects
    Given The HMG Role Admin wants to get All Project with API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    When the HMG Role Admin requests to get all Project
    Then the request is successfully with response code 200
    And a list of all Project is returned in the response
      """json
      {
         "results": [
            {
               "key": "hmg-notice-read",
               "name": "HMG Notice"
            }
         ],
         "metadata": {
            "totalCount": 1,
            "totalPageCount": 1,
            "size": 10
         }
      }
      """

  Scenario: Positive: Get a Project by keyword search
    Given The HMG Role Admin wants to get Project with the keyword search 'notice'
    When the HMG Role Admin request to get Project by specific keyword
    Then the request is successfully with response code 200
    And a list of all Project is returned in the response
      """json
      {
         "results": [
            {
               "key": "hmg-notice-read",
               "name": "HMG Notice"
            }
         ],
         "metadata": {
            "totalCount": 1,
            "totalPageCount": 1,
            "size": 10
         }
      }
      """

  Scenario: Negative: Get a Project by keyword search there's not in the list
    Given The HMG Role Admin wants to get Project with the keyword search 'role'
    When the HMG Role Admin request to get Project by specific keyword
    Then the request is successfully with response code 200
    And a list of all Project is returned empty in the response
      """json
      {
         "results": [],
         "metadata": {
            "totalCount": 0,
            "totalPageCount": 0,
            "size": 10
         }
      }
      """

  Scenario: Positive: Get a Project by key
    Given HMG Role Admin wants to retrieve a Project with the key 'hmg-notice-read' and API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    When the HMG Role Admin requests to get Project by key
    Then the request is successfully with response code 200
    And return the Project
      """json
      {
         "key": "hmg-notice-read",
         "name": "HMG Notice"
      }
      """

  Scenario: Negative: Get a Project by a non-existent Key
    Given HMG Role Admin wants to retrieve a Project with the key 'wrong_key' and API key '22ee17bd-699e-4b6b-a30b-e854e6c576c5'
    When the HMG Role Admin requests to get Project by key
    Then the request is fails with response code 404
    And the response body read includes status 404 and error message 'Not Found'
