package com.kgignatyev.fss.service.security.impl

import com.kgignatyev.fss.service.security.UserDetailsImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service("fssUserDetailsService")
class FssUserDetailsSvcImpl : UserDetailsService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        logger.info("Loading user by username: $username")
        return UserDetailsImpl()
    }
}
