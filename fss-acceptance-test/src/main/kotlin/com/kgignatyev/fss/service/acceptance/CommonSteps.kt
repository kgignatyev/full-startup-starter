package com.kgignatyev.fss.service.acceptance


import com.kgignatyev.fss.service.acceptance.TestsContext.anonymousUser
import com.kgignatyev.fss.service.acceptance.data.CfgValues
import com.kgignatyev.fss.service.acceptance.security.SecurityHelper
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.kotest.matchers.ints.shouldBeGreaterThan
import jakarta.annotation.Resource
import java.io.FileWriter


class CommonSteps {

    @Resource
    lateinit var authenticationInterceptor: AuthenticationInterceptor

    @Resource
    lateinit var securityHelper: SecurityHelper

    @Resource lateinit var cfg: CfgValues

    @Given("user {string}")
    fun given_user(userName: String?) {
        if( "guest" == userName || "anonymous" == userName) {
            TestsContext.currentUser = anonymousUser
        }else {
            TestsContext.currentUser = securityHelper.getUser(userName)
        }
    }

    @Given("user {string} is logged in")
    fun user_is_logged_in(userName: String?) {
        given_user(userName)
        val userToken = authenticationInterceptor.getToken()
        userToken.length shouldBeGreaterThan 10
    }

    @Then("write custom parameters file for cluecumber")
    fun write_custom_parameters_file_for_cluecumber() {
        val system = System.getProperty("os.name")
        val runtime = Runtime.getRuntime()
        val cpus = runtime.availableProcessors()
        val memory = runtime.maxMemory()
        val freeMemory = runtime.freeMemory()

        FileWriter("target/test_info.properties").use { wr->
            wr.write("url=${cfg.fssApiBaseUrl}\n")
            wr.write("test_machine=$cpus CPUs, $memory max memory, $freeMemory free memory, $system\n")
        }
    }


}
