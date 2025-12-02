package com.kgignatyev.fss.service.security


class CallerInfo {
    var currentUser:User = anonymousUser
    var realUser:User = anonymousUser // when impersonation is used

    companion object {

        //headers are normalized to lowercase by infrastructure
        const val X_IMPERSONATE = "x-impersonate"

        val anonymousUser = User().apply {
            id = ""
            name = "anonymous"
            email = "anonymous@some.com"
        }
        val adminUser = User().apply {
            id = "admin"
            name = "admin"
            email = "admin@some.com"
        }
        val anonymousCaller = CallerInfo()
    }

    override fun toString(): String {
        return "CallerInfo(currentUser=$currentUser, realUser=$realUser)"
    }
}



