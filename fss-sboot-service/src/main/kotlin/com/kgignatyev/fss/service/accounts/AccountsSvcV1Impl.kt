package com.kgignatyev.fss.service.accounts

import com.kgignatyev.fss.service.common.api.APIHelpers.ofOptional
import com.kgignatyev.fss.service.common.utils.DataUtils.assignIfNotNull
import com.kgignatyev.fss_svc.api.fsssvc.AccountsServiceV1Api
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.*
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController


@RequestMapping(path = ["/api"])
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"], methods = [RequestMethod.PATCH, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.PUT])
@RestController
class AccountsSvcV1Impl( val accountsSvc: AccountsSvc, val conversionService: ConversionService,
    ): AccountsServiceV1Api {

    override fun searchAccounts(v1SearchRequest: V1SearchRequest): ResponseEntity<V1AccountListResult> {
        val r = accountsSvc.search(v1SearchRequest.searchExpression,v1SearchRequest.sortExpression, v1SearchRequest.pagination.offset, v1SearchRequest.pagination.limit)
        val res = V1AccountListResult()
        res.items = r.items.map { conversionService.convert(it, V1Account::class.java)!! }
        res.listSummary = r.summary.toApiListSummary()
        res.listSummary.size = res.items.size
        return ResponseEntity.ok( res )
    }

    override fun createAccount(v1Account: V1Account): ResponseEntity<V1Account> {
        val a = conversionService.convert(v1Account, Account::class.java)!!
        val res = accountsSvc.save(a)
        return ResponseEntity.ok( conversionService.convert(res, V1Account::class.java))
    }

    override fun getAccountById(accountId: String): ResponseEntity<V1Account> {
        val aO = accountsSvc.findById(accountId)
        return ofOptional(aO){
            conversionService.convert(it, V1Account::class.java)!!
        }
    }

    override fun updateAccountById(accountId: String, v1Account: V1AccountUpdateCmd): ResponseEntity<V1Account> {
        val a = accountsSvc.findById(accountId).get()
        applyUpdate( a, v1Account )
        val res = accountsSvc.save(a)
        return ResponseEntity.ok( conversionService.convert(res, V1Account::class.java))
    }

    private fun applyUpdate(
        a: Account,
        u: V1AccountUpdateCmd
    ) {
        assignIfNotNull<Account, String>(a, u.name ){ cv ->
            name = cv
        }
        assignIfNotNull<Account, String>(a, u.notes ){ cv ->
            notes = cv
        }
    }

    override fun deleteAccountById(accountId: String): ResponseEntity<V1Status> {
        accountsSvc.deleteById(accountId)
        return ResponseEntity.ok(V1Status().responseCode("OK"))
    }
}
