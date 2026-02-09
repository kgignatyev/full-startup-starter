@create-account
Feature: Account management


Scenario Outline: Create a new account
  Given user "<userName>" is logged in
  And current user does not have an account
  And current user is not stored in the system
  Then current user can create a new account with the name "<accountName>"
  And current user can see data of account "<accountName>"
  And current user is stored in the system
  And current user policies allow user to manage account "<accountName>"
  Examples:
    | userName  | accountName |
    | testUser1 | Test1   |
    | testUser2 | Test2   |

Scenario Outline: manage account
  Given user "<userName>" is logged in
  Then current user can update data of account "<ownAccountName>"
  And current user can NOT see data of account "<otherAccountName>"
  And current user can NOT update data of account "<otherAccountName>"

  Examples:
    | userName  | ownAccountName | otherAccountName |
    | testUser1 | Test1   | Test2   |
    | testUser2 | Test2   | Test1   |
