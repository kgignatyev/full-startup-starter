package com.kgignatyev.fss.service.security

import com.kgignatyev.fss.service.UnauthorizedException
import com.kgignatyev.fss.service.common.events.CrudEventType
import java.util.*


interface SecuritySvc {
    fun getSecurityPoliciesForUser(userId: String): List<SecurityPolicy>
    fun ensureCurrentUserIsStored()
    fun getCallerInfo(): CallerInfo
    fun deleteUser(userId: String)
    fun onCrudEvent(o:Securable,eventType: CrudEventType)
    fun isCurrentUserAuthorized( o: Securable, action: String): Boolean
    @Throws(UnauthorizedException::class)
    fun checkCurrentUserAuthorized( o: Securable, action: String)
    fun isUserAuthorized( userId:String,o: Securable, action: String): Boolean
    fun getUserById(userId: String): Optional<User>
    fun updateUserById(u: User): User
}
