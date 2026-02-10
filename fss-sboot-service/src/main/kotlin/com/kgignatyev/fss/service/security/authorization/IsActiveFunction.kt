package com.kgignatyev.fss.service.security.authorization

import com.googlecode.aviator.runtime.type.AviatorBoolean
import com.googlecode.aviator.runtime.type.AviatorObject
import org.casbin.jcasbin.util.function.CustomFunction
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class IsActiveFunction: CustomFunction() {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun getName(): String {
        return "isActive"
    }

    override fun call(env: MutableMap<String, Any>, caller: AviatorObject): AviatorObject {
        //here we can check if the user is active
        return AviatorBoolean.TRUE
    }
}
