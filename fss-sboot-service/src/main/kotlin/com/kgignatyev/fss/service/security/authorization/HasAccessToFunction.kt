package com.kgignatyev.fss.service.security.authorization

import com.googlecode.aviator.runtime.type.AviatorBoolean
import com.googlecode.aviator.runtime.type.AviatorObject
import com.kgignatyev.fss.service.security.Securable
import com.kgignatyev.fss.service.security.SecurableType
import org.casbin.jcasbin.util.function.CustomFunction
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class HasAccessToFunction : CustomFunction() {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)


    override fun getName(): String {
        return "hasAccessTo"
    }

    override fun call(
        env: MutableMap<String, Any>,
        policyObj: AviatorObject,
        requestObj: AviatorObject
    ): AviatorObject {
        val pObj = policyObj.getValue(env) as String
        val rObj = requestObj.getValue(env) as Securable
        val res = when (pObj) {
            "*" ->  AviatorBoolean.TRUE
            "" ->  AviatorBoolean.FALSE
            else -> {

                val (type, id) = pObj.split("/")
                when (type) {
                    SecurableType.ACCOUNT.tName -> {
                        val accountId = accountIdOf(rObj)
                        if( "*" == id || accountId == id) {
                            AviatorBoolean.TRUE
                        }else{
                            AviatorBoolean.FALSE
                        }
                    }
                    SecurableType.USER.tName -> {
                        val uId = rObj.id()
                        if( "*" == id || uId == id) {
                            AviatorBoolean.TRUE
                        }else{
                            AviatorBoolean.FALSE
                        }
                    }

                    else -> {
                        throw Exception("Unknown object type: $type")
                    }
                }
            }
        }
        if (logger.isDebugEnabled) {
            logger.debug(
                "$name = $res  policyObj = [$pObj], requestObj = [${rObj}]"
            )
        }
        return res
    }

    private fun accountIdOf(rObj: Any?): String {
        when(rObj) {
            is Securable -> {
                return rObj.accountId()
            }
            else -> {
                throw Exception("Unknown object type: ${rObj?.javaClass?.name}")
            }
        }
    }

}
