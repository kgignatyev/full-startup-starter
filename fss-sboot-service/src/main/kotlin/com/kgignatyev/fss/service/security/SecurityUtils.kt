package com.kgignatyev.fss.service.security

import com.kgignatyev.fss.service.security.impl.SecurityContext


object SecurityUtils {

    fun <T> doAsAdmin(f: () -> T): T {
        val realCallerInfo: CallerInfo? = SecurityContext.callerInfo.get()
        val adminCaller = if (realCallerInfo == null) {
            CallerInfo().apply {
                currentUser = CallerInfo.adminUser
                realUser = CallerInfo.adminUser
            }
        } else {
            CallerInfo().apply {
                currentUser = CallerInfo.adminUser
                realUser = realCallerInfo.realUser
            }
        }
        try {
            SecurityContext.callerInfo.set(adminCaller)
            return f()
        } finally {
            SecurityContext.callerInfo.set(realCallerInfo)
        }
    }
}
