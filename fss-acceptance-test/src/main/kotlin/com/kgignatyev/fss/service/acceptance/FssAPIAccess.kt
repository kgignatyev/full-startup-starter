package com.kgignatyev.fss.service.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.kgignatyev.fss.service.acceptance.TestsContext.anonymousUser
import com.kgignatyev.fss.service.acceptance.data.CfgValues
import com.kgignatyev.fss_svc.api.fss_client.v1.apis.AccountsServiceV1Api
import com.kgignatyev.fss_svc.api.fss_client.v1.apis.CompaniesServiceV1Api
import com.kgignatyev.fss_svc.api.fss_client.v1.apis.JobsServiceV1Api
import com.kgignatyev.fss_svc.api.fss_client.v1.apis.SecurityServiceV1Api
import com.kgignatyev.fss_svc.api.fss_client.v1.models.V1Pagination
import com.kgignatyev.fss_svc.api.fss_client.v1.models.V1SearchRequest
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.io.File

object ImpersonationHelper {
    val impersonateUserId = ThreadLocal<String>()

    fun <T>runAsUserWithId( userId: String, f:()->T ): T {
        try{
            impersonateUserId.set(userId)
            return f()
        }finally {
            impersonateUserId.set("")
        }
    }
}

@Component
class AuthenticationInterceptor(val cfg: CfgValues, val om:ObjectMapper) : Interceptor {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)


    val auth0ApiClient: OkHttpClient= OkHttpClient.Builder()
        .build()


    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().signedRequest()
        return chain.proceed(newRequest)
    }

    private fun Request.signedRequest(): Request {
        val builder = this.newBuilder()
            .header("Authorization", "Bearer ${getToken()}")
        val userIdForImpersonation = ImpersonationHelper.impersonateUserId.get()
        if( org.springframework.util.StringUtils.hasText( userIdForImpersonation )) {
            builder.header("X-Impersonate", userIdForImpersonation)
        }
        return builder.build()
    }

    private val tokenGuard = "TokenGuard"

    fun getToken():String {
        if(TestsContext.currentUser == anonymousUser) {
            return "-dummy-token-for-anonymous"
        }
        synchronized(tokenGuard) {
            //we use file to store token, so we don't have to request it every time
            //Auth0 has limit on number of requests
            val u = TestsContext.currentUser
            val tokenFile = File(".secrets/${u.name}.txt")
            var token = if (tokenFile.exists()) {
                val t = tokenFile.readText()
                if (isExpired(t)) {
                    null
                } else {
                    t
                }
            } else {
                null
            }
            if (token == null) {
                val parent = tokenFile.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                }
                token = requestToken(u.name, u.password)
                tokenFile.writeText(token)
            }
            return token
        }
    }

    private fun isExpired(jwt: String): Boolean {
        val parts = jwt.split(".")
        val payload = String( Base64.decodeBase64(parts[1]) )
        val exp = om.readTree(payload).get("exp").asLong()
        return  exp < System.currentTimeMillis() / 1000
    }

    fun requestToken(user_name:String, user_password:String):String {

        val body  = FormBody.Builder()
            .add("grant_type", "password")
            .add("username", user_name)
            .add("password", user_password)
            .add("audience", cfg.fssApiAudience)
            .add("scope", "openid profile email")
            .add("realm", "Username-Password-Authentication")
            .add("client_id", cfg.fssApiClientId)
            .add("client_secret", cfg.fssApiClientSecret)
            .build()
        val r = Request.Builder()
            .header("ContentType", "application/x-www-form-urlencoded")
            .url("${cfg.auth0issuer}/oauth/token")
            .post(body)
            .build()
        val buffer = okio.Buffer()
        r.body!!.writeTo(buffer)
        logger.debug("Request body: ${buffer.readUtf8()}")
        auth0ApiClient.newCall(r).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")
            val json = response.body!!.string()
            val jsonTree = om.readTree( json)
            return jsonTree.get("access_token").asText()
        }
    }



}

@Configuration
class FssAPIAccess(val cfg: CfgValues) {



    @Bean
    fun fssApiClient(authenticationInterceptor: AuthenticationInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(authenticationInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Bean
    fun jobsApi(apiClient: OkHttpClient): JobsServiceV1Api {
        return JobsServiceV1Api(cfg.fssApiBaseUrl,apiClient)
    }

    @Bean
    fun companiesApi(apiClient: OkHttpClient):CompaniesServiceV1Api {
        return CompaniesServiceV1Api(cfg.fssApiBaseUrl,apiClient)
    }

    @Bean
    fun accountsApi(apiClient: OkHttpClient):AccountsServiceV1Api {
        return AccountsServiceV1Api(cfg.fssApiBaseUrl,apiClient)
    }

    @Bean
    fun securityApi(apiClient: OkHttpClient): SecurityServiceV1Api {
        return SecurityServiceV1Api(cfg.fssApiBaseUrl,apiClient)
    }

}

object ApiHelpers {
    fun createSearchRequest( filter: String, sort: String, offset:Long = 0, limit:Int= 10): V1SearchRequest {
        val pagination = V1Pagination(offset, limit)
        return V1SearchRequest( filter, sort, pagination)
    }
}
