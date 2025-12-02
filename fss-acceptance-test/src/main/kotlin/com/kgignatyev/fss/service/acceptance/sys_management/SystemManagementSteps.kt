package com.kgignatyev.fss.service.acceptance.sys_management

import com.auth0.json.mgmt.userAttributeProfiles.UserId
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kgignatyev.fss.service.acceptance.ApiHelpers.createSearchRequest
import com.kgignatyev.fss.service.acceptance.ImpersonationHelper
import com.kgignatyev.fss.service.acceptance.data.CfgValues
import com.kgignatyev.fss.service.acceptance.security.SecurityHelper
import com.kgignatyev.fss_svc.api.fss_client.v1.apis.AccountsServiceV1Api
import com.kgignatyev.fss_svc.api.fss_client.v1.apis.SecurityServiceV1Api
import com.kgignatyev.fss_svc.api.fss_client.v1.models.V1Pagination
import com.kgignatyev.fss_svc.api.fss_client.v1.models.V1SearchRequest
import com.kgignatyev.fss_svc.api.fss_client.v1.models.V1SecurityPolicy
import io.cucumber.java.PendingException
import io.cucumber.java.en.Then
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import jakarta.annotation.Resource
import org.apache.commons.io.IOUtils
import org.junit.Assert
import java.net.URI
import java.net.URL
import java.nio.charset.Charset


class SystemManagementSteps {

    @Resource
    lateinit var accountsApi: AccountsServiceV1Api
    @Resource
    lateinit var securitySvc: SecurityServiceV1Api

    @Resource
    lateinit var securityHelper: SecurityHelper

    @Resource
    lateinit var cfg: CfgValues

    @Resource
    lateinit var om: ObjectMapper

    @Then("current user can search accounts")
    fun admin_user_can_search_accounts() {
        val r = createSearchRequest( "name like '%t%'",
            "name asc")
        val res = accountsApi.searchAccounts( r )
        val accountOwners= res.items.map { it.ownerId }.toSet()
        accountOwners.size shouldBeGreaterThan 1
    }

    @Then("current user can impersonate normal user {string}")
    fun admin_user_can_impersonate_normal_user(userName: String) {
        val testUser = securityHelper.getUser(userName)
        val users = securitySvc.searchUsers(V1SearchRequest(
            searchExpression = "name = '${testUser.name}'",
            sortExpression = "name asc",
            pagination = V1Pagination(0,10)
        ))
        val user = users.items[0]
        //when we make impersonation call we get policies of the user, and not admin
        ImpersonationHelper.runAsUserWithId(user.id) {
            val policies = securitySvc.getSecurityPoliciesForUser("me")
            allPoliciesAreForUserId( user.id, policies)
        }

    }

    private fun allPoliciesAreForUserId(
        userId:  String,
        securityPolicies: List<V1SecurityPolicy>
    ) {
        val userIdsInPolicies = securityPolicies.map { it.userId }.toSet()
        Assert.assertEquals(1, userIdsInPolicies.size)
        val policiesForUserId = userIdsInPolicies.first()
        Assert.assertEquals(userId, policiesForUserId)
    }

    @Then( "health checkpoint is available and is UP")
    fun health_checkpoint_is_available_and_up(){
        val healthUrl = cfg.fssApiBaseUrl.replace("/api","/actuator/health")
        val healthData = om.readValue(URI.create(healthUrl).toURL().openStream(), JsonNode::class.java)
        println(healthData)
        healthData.get("status").asText() shouldBe "UP"
    }

    @Then( "prometheus metrics are available")
    fun prometheus_metrics_are_available(){
        val prometheusMetrics = cfg.fssApiBaseUrl.replace("/api","/actuator/prometheus")
        val metricLines = URI.create(prometheusMetrics).toURL().openStream().use { stream ->
             IOUtils.readLines(stream, Charset.defaultCharset())
        }
        //println(metricLines)
        metricLines.size shouldBeGreaterThan 10
    }
}
