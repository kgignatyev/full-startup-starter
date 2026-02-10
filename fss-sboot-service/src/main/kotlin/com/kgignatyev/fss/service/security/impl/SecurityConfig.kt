package com.kgignatyev.fss.service.security.impl

import com.kgignatyev.fss.service.security.CallerInfo
import io.opentelemetry.context.ContextKey
import jakarta.annotation.Resource
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import java.util.Locale.getDefault


@Configuration
class SecurityConfig {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Resource
    @Qualifier("fssUserDetailsService")
    lateinit var userDetailsService: UserDetailsService

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        /*
        This is where we configure the security required for our endpoints
        and set up our app to serve as
        an OAuth2 Resource Server, using JWT validation.
        */
        return http
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/api/v1/**").authenticated()
            }
            .cors(withDefaults())
            .oauth2ResourceServer { oauth2: OAuth2ResourceServerConfigurer<HttpSecurity?> ->
                oauth2
                    .jwt( withDefaults())

            }
            .csrf { csrf -> csrf.disable() }
            .userDetailsService(userDetailsService)
            .build()
    }
}

@Component
class SecurityHandlingFilter: Filter {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun doFilter(req: ServletRequest?, resp: ServletResponse?, chain: FilterChain?) {
        when (req) {
            is jakarta.servlet.http.HttpServletRequest -> {
                val headers = req.headerNames.toList().map { it.lowercase() to req.getHeader(it) }
                logger.info("${Thread.currentThread()} headers: $headers")
                SecurityContext.httpHeaders.set(headers.toMap())
                }
              else ->
                logger.warn("Unsupported request type: ${req?.javaClass?.name}")

        }
        try {
            chain?.doFilter(req, resp)
        }finally {
            logger.info("${Thread.currentThread()} reset headers")
            SecurityContext.httpHeaders.set(emptyMap())
            SecurityContext.callerInfo.set(null)
        }
    }
}

object SecurityContext {
    fun getHeader(hKey: String): String? {
        return httpHeaders.get()[hKey.lowercase()]
    }

    //all header keys are in the 'lowercase'
    val httpHeaders = ThreadLocal.withInitial<Map<String, String>> { emptyMap() }
    val callerInfo:ThreadLocal<CallerInfo?> = ThreadLocal()
}
