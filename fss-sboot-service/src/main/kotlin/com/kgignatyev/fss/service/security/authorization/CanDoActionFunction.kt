package com.kgignatyev.fss.service.security.authorization

import com.googlecode.aviator.runtime.type.AviatorBoolean
import com.googlecode.aviator.runtime.type.AviatorObject
import org.casbin.jcasbin.util.function.CustomFunction
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class CanDoActionFunction : CustomFunction() {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun getName(): String {
        return "canDoAction"
    }
    fun toSet(policy: String): Set<String> {
        return policy.split(" ").filter { it.isNotEmpty() }.map { it.trim() }.toSet()
    }

    override fun call(
        env: MutableMap<String, Any>,
        policyAction: AviatorObject,
        targetAction: AviatorObject
    ): AviatorObject {
        val pActionSet = toSet(policyAction.getValue(env) as String)
        val tAction = targetAction.getValue(env) as String
        val r  =  if (pActionSet.contains("*") || pActionSet.contains(tAction)) {
            AviatorBoolean.TRUE
        } else {
            AviatorBoolean.FALSE
        }
        if (logger.isDebugEnabled) {
            logger.debug(
                "$name = $r  policyAction = [$pActionSet], targetAction = [${tAction}]"
            )
        }
        return r
    }

}
