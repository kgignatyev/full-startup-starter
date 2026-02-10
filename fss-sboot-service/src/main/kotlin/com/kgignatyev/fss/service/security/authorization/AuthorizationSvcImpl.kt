package com.kgignatyev.fss.service.security.authorization

import com.kgignatyev.fss.service.security.AuthorizationSvc
import com.kgignatyev.fss.service.security.storage.SecurityPoliciesRepo
import org.apache.commons.io.IOUtils
import org.casbin.jcasbin.main.Enforcer
import org.casbin.jcasbin.model.Model
import org.casbin.jcasbin.persist.Helper
import org.casbin.jcasbin.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
class AuthorizationSvcImpl(val policiesRepo: SecurityPoliciesRepo, val cacheManager: CacheManager) : AuthorizationSvc {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${casbin.enable-log}")
    var enableCasbinLogging: Boolean = false

    val canDoAction = CanDoActionFunction()
    val isActiveFunction = IsActiveFunction()
    val belongsToAccountFunction = HasAccessToFunction()


    override fun getEvaluatorExpression(): String {
        val authorizationModelText = resourceAsText("fss_authorization_model.conf")
        val authzModelLines = authorizationModelText.split("\n")
        val prefix = "m ="
        val evalLine = authzModelLines.find { it.startsWith(prefix) }!!
        return evalLine.substring(prefix.length).trim()
    }

    fun createEnforcerForUser(userId: String): Enforcer {
        logger.info("Creating enforcer for user $userId")
        val model = Model()
        val authorizationModelText = resourceAsText("fss_authorization_model.conf")
        model.loadModelFromText(authorizationModelText)
        val policies = policiesRepo.findByUserId(userId)
        policies.forEach { policy ->
            val pLine = "p, ${policy.userId}, ${policy.policy}"
            Helper.loadPolicyLine(pLine, model)
        }
        val enforcer = Enforcer(model)
        postConfigureEnforcer(enforcer)
        return enforcer
    }

    override fun postConfigureEnforcer(enforcer: Enforcer) {
        enforcer.addFunction(canDoAction.name, canDoAction)
        enforcer.addFunction(isActiveFunction.name, isActiveFunction)
        enforcer.addFunction(belongsToAccountFunction.name, belongsToAccountFunction)
        Util.enableLog = enableCasbinLogging
    }

    private fun resourceAsText(resource: String): String {
        val inputStream = this.javaClass.classLoader.getResourceAsStream(resource)
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name())
    }

    @Cacheable("enforcers-global")
    override fun getEnforcerForUser(userId: String): Enforcer {
        val e = createEnforcerForUser(userId)
        return e
    }


    @CacheEvict("enforcers-global", allEntries = true)
    override fun evictEnforcers() {
        logger.info("Evicting enforcers cache")
    }
}
