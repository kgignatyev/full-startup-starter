package com.kgignatyev.fss.service.acceptance.account

import com.kgignatyev.fss.service.acceptance.ApiHelpers.createSearchRequest
import com.kgignatyev.fss.service.acceptance.TestsContext
import com.kgignatyev.fss.service.acceptance.security.SecurityHelper
import com.kgignatyev.fss_svc.api.fss_client.v1.apis.AccountsServiceV1Api
import com.kgignatyev.fss_svc.api.fss_client.v1.apis.SecurityServiceV1Api
import com.kgignatyev.fss_svc.api.fss_client.v1.infrastructure.ClientException
import com.kgignatyev.fss_svc.api.fss_client.v1.models.V1Account
import com.kgignatyev.fss_svc.api.fss_client.v1.models.V1AccountUpdateCmd
import com.kgignatyev.fss_svc.api.fss_client.v1.models.V1YN
import io.cucumber.java.PendingException
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveMinLength
import jakarta.annotation.Resource
import java.time.OffsetDateTime


class AccountSteps {

    @Resource
    lateinit var accountsAPI: AccountsServiceV1Api

    @Resource
    lateinit var securityAPI: SecurityServiceV1Api

    @Resource
    lateinit var accountHelpers: AccountHelpers

    @Resource
    lateinit var securityHelper: SecurityHelper

    @And("current user is not stored in the system")
    fun current_user_is_not_stored_in_the_system() {
        userExists(TestsContext.currentUser.name) shouldBe false
    }

    fun userExists(userName: String): Boolean {
        val foundUsers = securityHelper.runAsUser("admin") {
            securityAPI.searchUsers(
                createSearchRequest("name = '$userName'", "name asc")
            ).items
        }
        return if (foundUsers.isEmpty()) {
            false
        } else {
            foundUsers.size shouldBeExactly 1
            true
        }
    }

    @And("current user is stored in the system")
    fun current_user_is_stored_in_the_system() {
        userExists(TestsContext.currentUser.name) shouldBe true
    }

    @Then("current user can create a new account with the name {string}")
    fun current_user_can_create_a_new_account_with_the_name(accountName: String) {
        val a = V1Account("", "", accountName, OffsetDateTime.now(), V1YN.Y)
        val account = accountsAPI.createAccount(a)
        account.name shouldBe accountName
        account.id shouldHaveMinLength 2
    }

    @Then("current user policies allow user to manage account {string}")
    fun current_user_policies_allow_user_to_manage_account(accountName: String) {
        val accounts = accountHelpers.findAccountsByName(accountName).items
        accounts.size shouldBeExactly 1
        val account = accounts[0]
        val policies = securityAPI.getSecurityPoliciesForUser("my")
        val policy = policies.find { it.policyExpression.contains( "Account/${account.id}, *") }
        policy shouldNotBe null
    }
    @Then("current user can see data of account {string}")
    fun current_user_can_see_data_of_account(accountName: String) {
        val accounts = accountHelpers.findAccountsByName(accountName).items
        accounts.size shouldBeExactly 1
        val account = accounts[0]
        account.name shouldBe accountName
    }

    @Then("current user can update data of account {string}")
    fun current_user_can_update_data_of_account(accountName: String) {
        val accounts = accountHelpers.findAccountsByName(accountName).items
        accounts.size shouldBeExactly 1
        val account = accounts[0]
        account.name shouldBe accountName
        val updateAccountCmd = V1AccountUpdateCmd(
            name = account.name,
            notes = "new notes ${OffsetDateTime.now()}"
        )
        val updatedFromServer = accountsAPI.updateAccountById(account.id, updateAccountCmd)
        updatedFromServer.notes shouldBe updateAccountCmd.notes
    }

    @Then("current user can NOT see data of account {string}")
    fun current_user_can_not_see_data_of_account(accountName: String) {
        val accounts = accountHelpers.findAccountsByName(accountName).items
        accounts.size shouldBeExactly 0

        val accountsForAdmin = securityHelper.runAsUser("admin") {
            accountHelpers.findAccountsByName(accountName).items
        }
        accountsForAdmin.size shouldBeExactly 1
        val account = accountsForAdmin[0]
        var a: V1Account? = null
        try {
            a = accountsAPI.getAccountById(account.id)
        } catch (e: Exception) {

            when (e) {
                is ClientException -> {
                    e.statusCode shouldBe 403
                    return
                }

                else -> {
                    throw e
                }
            }
        }
    }

    @Then("current user can NOT update data of account {string}")
    fun current_user_can_not_update_data_of_account(accountName: String) {
        val accountsForAdmin = securityHelper.runAsUser("admin") {
            accountHelpers.findAccountsByName(accountName).items
        }
        accountsForAdmin.size shouldBeExactly 1
        val account = accountsForAdmin[0]
        val updateCmd = V1AccountUpdateCmd(
            name = account.name,
            notes = "new notes ${OffsetDateTime.now()}"
        )
        try {
            accountsAPI.updateAccountById(account.id, updateCmd)
            throw Exception("Should not be able to update account $accountName")
        } catch (e: Exception) {
            when (e) {
                is ClientException -> {
                    e.statusCode shouldBe 403
                    return
                }

                else -> {
                    throw e
                }
            }
        }
    }

    @Given("current user has account {string}")
    fun current_user_has_account(accountName: String) {
        val a = accountsAPI.getAccountById("my")
        a.name shouldBe accountName
    }

    @Given("current user does not have an account")
    fun current_user_does_not_have_an_account() {
        try {
            val a = accountsAPI.getAccountById("my")
            println("Found unexpected account: $a")
        } catch (e: Exception) {
            when (e) {
                is ClientException -> {
                    e.statusCode shouldBe 404
                    return
                }

                else -> {
                    throw e
                }
            }
        }
        throw Exception("Account should not exist")

    }


}
