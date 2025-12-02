package com.kgignatyev.fss.service.security

import com.kgignatyev.fss.service.common.api.APIHelpers.ofOptional
import com.kgignatyev.fss.service.common.api.V1StatusHelpers.OK_ENTITY
import com.kgignatyev.fss_svc.api.fsssvc.SecurityServiceV1Api
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["/api"])
@CrossOrigin(
    origins = ["*"],
    allowedHeaders = ["*"],
    methods = [RequestMethod.PATCH, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.PUT]
)
@RestController
class SecuritySvcV1Impl(
    val securitySvc: SecuritySvc,
    val userSvc: UserSvc,
    val conversionService: ConversionService
) : SecurityServiceV1Api {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun searchUsers(v1SearchRequest: V1SearchRequest): ResponseEntity<V1UsersListResult> {
        val r = userSvc.search(
            v1SearchRequest.searchExpression,
            v1SearchRequest.sortExpression,
            v1SearchRequest.pagination.offset,
            v1SearchRequest.pagination.limit
        )
        val res = V1UsersListResult()
        res.items = r.items.map { conversionService.convert(it, V1User::class.java) }
        res.listSummary = r.summary.toApiListSummary()
        return ResponseEntity.ok(res)
    }

    override fun getSecurityPoliciesForUser(userId: String): ResponseEntity<List<V1SecurityPolicy>> {
        val effectiveUserId =
            if (userId == "my" || userId == "me") {
                val callerInfo = securitySvc.getCallerInfo()
                callerInfo.currentUser.id
            } else userId
        val policies: List<SecurityPolicy> = try {
            securitySvc.getSecurityPoliciesForUser(effectiveUserId)
        } catch (ex: Exception) {
            logger.warn("Error while retrieving security policies for user $effectiveUserId, returning guest user policies", ex)
            SecurityUtils.doAsAdmin {
                securitySvc.getSecurityPoliciesForUser("guest")
            }
        }
        return ResponseEntity.ok(policies.map { conversionService.convert(it, V1SecurityPolicy::class.java)!! })
    }

    override fun deleteUserById(userId: String): ResponseEntity<V1Status> {
        securitySvc.deleteUser(userId)
        return OK_ENTITY
    }

    override fun getUserById(userId: String): ResponseEntity<V1User> {
        return ofOptional(securitySvc.getUserById(userId)) { conversionService.convert(it, V1User::class.java)!! }
    }

    override fun updateUserById(userId: String, v1User: V1User): ResponseEntity<V1User> {
        v1User.id = userId
        val u = conversionService.convert(v1User, User::class.java)!!
        val updatedUser = securitySvc.updateUserById(u)
        return ResponseEntity.ok(conversionService.convert(updatedUser, V1User::class.java)!!)
    }
}
