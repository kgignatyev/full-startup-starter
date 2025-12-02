package com.kgignatyev.fss.service.security.svc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.kgignatyev.fss.service.BadRequestException
import com.kgignatyev.fss.service.UnauthorizedException
import com.kgignatyev.fss.service.common.data.Operation.DELETE
import com.kgignatyev.fss.service.common.data.Operation.IMPERSONATE
import com.kgignatyev.fss.service.common.data.Operation.UPDATE
import com.kgignatyev.fss.service.common.events.CrudEventType
import com.kgignatyev.fss.service.security.*
import com.kgignatyev.fss.service.security.storage.SecurityPoliciesRepo
import jakarta.annotation.Resource
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*


@Service
class SecuritySvcImpl(
    val userSvc: UserSvc,
    val policiesRepo: SecurityPoliciesRepo,
    val authorizationSvc: AuthorizationSvc,
) : SecuritySvc {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${okta.oauth2.issuer}")
    lateinit var issuer: String

    @Resource
    lateinit var om:ObjectMapper


    @Transactional
    override fun onCrudEvent(o:Securable,eventType: CrudEventType) {
        logger.info("Crud event: ${o.type()} $eventType")
        if( eventType == CrudEventType.CREATED){
            when(o.type()){
                 SecurableType.ACCOUNT -> {
                    val ownerId = o.ownerId()
                    val policy = SecurityPolicy()
                    policy.userId = ownerId
                    policy.name = "account-owner"
                    policy.policy = "${o.type().tName}/${o.id()}, *"
                    policiesRepo.save(policy)
                    authorizationSvc.evictEnforcers()
                }
                else -> {}
            }
        }
    }

    override fun getSecurityPoliciesForUser(userId: String): List<SecurityPolicy> {
        val uO = userSvc.findById(userId)
        if (uO.isEmpty) {
            throw IllegalArgumentException("User not found")
        }
        checkCurrentUserAuthorized(uO.get(), "read")
        return policiesRepo.findByUserId(userId)
    }

    override fun ensureCurrentUserIsStored() {
        val callerInfo = getCallerInfo()
        val user = callerInfo.currentUser
        if (user == CallerInfo.anonymousUser) {
            throw IllegalStateException("Anonymous user is not allowed to perform this operation")
        }
        val userO = userSvc.findByJwtSub(user.jwtSub)
        if (userO.isEmpty) {
            val savedUser = userSvc.save(user)
            val policy = SecurityPolicy()
            policy.userId = savedUser.id
            policy.name = "self access"
            policy.policy = "${SecurableType.USER.tName}/${savedUser.id}, *"
            policiesRepo.save(policy)
            authorizationSvc.evictEnforcers()
            callerInfo.currentUser = savedUser
        }

    }

    fun getUserInfoFromAuth0():User{
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.set("authorization", SecurityContext.httpHeaders.get()["authorization"]!!)
        val r = HttpEntity("", headers)
        val res = restTemplate.exchange("$issuer/userinfo", HttpMethod.GET,r,String::class.java)
        val info = om.readTree(  res.body ) as ObjectNode
        logger.debug("Received UserInfo: {}", info)
        val u = User()
        u.jwtSub = info.get("sub").asText()
        u.name = info.get("name").asText()
        u.email = info.get("email").asText()
        return u
    }

    override fun getCallerInfo(): CallerInfo {
        val auth = SecurityContextHolder.getContext().authentication
        logger.debug("principal:${auth.principal} \n\t details:${auth.details}")
        val principal = auth.principal
        val currentCallerInfo = SecurityContext.callerInfo.get()
        return if (currentCallerInfo != null) {
            return currentCallerInfo
        } else {
            val ci = when (principal) {
                is Jwt -> {
                    val realUserSub = principal.claims["sub"] as String
                    val userO = userSvc.findByJwtSub(realUserSub)
                    val realUser = if (userO.isEmpty) {
                        getUserInfoFromAuth0()
                    } else {
                        userO.get()
                    }
                    val callerInfo = CallerInfo()
                    val maybeImpersonate = SecurityContext.httpHeaders.get()[CallerInfo.X_IMPERSONATE]
                    if(maybeImpersonate != null) {
                        val userSecurable = User()
                        userSecurable.id = maybeImpersonate
                        if( isUserAuthorized( realUser.id , userSecurable , IMPERSONATE)){
                            val userToImpersonate = userSvc.findById(userSecurable.id).orElseThrow{ BadRequestException("User not found")}
                            callerInfo.currentUser = userToImpersonate
                        }else{
                            throw UnauthorizedException("User is not allowed to perform operation: $IMPERSONATE")
                        }
                    }else {
                        callerInfo.currentUser = realUser
                    }
                    callerInfo.realUser = realUser
                    callerInfo
                }

                else -> {
                    CallerInfo.anonymousCaller
                }
            }
            SecurityContext.callerInfo.set(ci)
            ci
        }
    }

    @Transactional
    override fun deleteUser(userId: String) {
        val u = User()
        u.id = userId
        checkCurrentUserAuthorized( u, DELETE)
        policiesRepo.deleteByUserId(userId)
        userSvc.deleteById(userId)
    }

    override fun getUserById(userId: String): Optional<User> {
        val effectiveUserId = if (userId == "my" || userId == "me") getCallerInfo().currentUser.id else userId
        val u = userSvc.findById(effectiveUserId)
        if( u.isPresent){
            checkCurrentUserAuthorized(u.get(), "read")
        }
        return u
    }

    override fun updateUserById(u: User): User {
        checkCurrentUserAuthorized(u, UPDATE)
        return userSvc.save(u)
    }

    override fun isUserAuthorized( userId:String, o: Securable, action: String): Boolean {
        val enforcer = authorizationSvc.getEnforcerForUser(userId)
        val enforce = enforcer.enforce(userId, o, action)
        if( logger.isDebugEnabled) {
            logger.debug("Enforcer: $enforce - Current user id: ${userId} - Securable $o - Action: $action ")
        }
        return enforce
    }

    override fun isCurrentUserAuthorized(o: Securable, action: String): Boolean {
        val userId =  getCallerInfo().currentUser.id
        return isUserAuthorized(userId,o,action)
    }

    override fun checkCurrentUserAuthorized(o: Securable, action: String) {
        if (!isCurrentUserAuthorized(o, action)) {
            throw UnauthorizedException("${getCallerInfo()} is not authorized to perform operation [$action] on object [${describeObjectForLog(o)}]")
        }
    }

    fun describeObjectForLog(o: Securable): String {
        return "${o.type().tName}/${o.id()} " + when (o.type()) {
            SecurableType.ACCOUNT -> "owned by ${o.ownerId()}"
            SecurableType.JOB -> " in account ${o.accountId()}"
            else -> ""
        }
    }
}
