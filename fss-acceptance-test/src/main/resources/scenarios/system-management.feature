@test-system-management
Feature: System management

  Scenario Outline: Admin impersonation ability
    Given user "admin"
    Then current user can search accounts
    And current user can impersonate normal user "<user>"
    Examples:
      | user      |
      | testUser1 |
      | testUser2 |

  Scenario: health data is available
    Given user "guest"
    Then health checkpoint is available and is UP

  Scenario: prometheus metrics are available
    Given user "guest"
    Then prometheus metrics are available


  Scenario: finalize tests
    Then write custom parameters file for cluecumber
