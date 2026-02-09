package com.kgignatyev.fss.service.accounts.svc

import com.kgignatyev.fss.service.accounts.Account
import com.kgignatyev.fss.service.accounts.AccountEvent
import com.kgignatyev.fss.service.accounts.AccountsSvc
import com.kgignatyev.fss.service.accounts.storage.AccountsRepo
import com.kgignatyev.fss.service.common.data.Operation
import com.kgignatyev.fss.service.common.data.Operation.READ
import com.kgignatyev.fss.service.common.data.SearchResult
import com.kgignatyev.fss.service.common.events.CrudEventType
import com.kgignatyev.fss.service.security.SecuritySvc
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
@Transactional
class AccountsSvcImpl( val _accountCrudRepo:AccountsRepo, val publisher: ApplicationEventPublisher,
    val securitySvc: SecuritySvc): AccountsSvc, CrudRepository<Account, String> by _accountCrudRepo{


    override fun search(searchExpr: String, sortExpr:String, offset: Long, limit: Int): SearchResult<Account> {
        val  r = searchImpl(searchExpr, sortExpr, offset, limit, _accountCrudRepo)
        val allowedAccounts = r.items.filter { securitySvc.isCurrentUserAuthorized(it, READ ) }
        return SearchResult( allowedAccounts, r.summary.copy(count = allowedAccounts.size))
    }

    @Transactional
    override fun <S : Account> save(entity: S ): S {
        securitySvc.ensureCurrentUserIsStored()
        var eventType = CrudEventType.UPDATED
        if( entity.id == "") {
            eventType = CrudEventType.CREATED
            entity.ownerId = securitySvc.getCallerInfo().currentUser.id
            entity.createdAt = OffsetDateTime.now()
        }else{
            securitySvc.checkCurrentUserAuthorized(entity, Operation.UPDATE)
        }
        entity.updatedAt = OffsetDateTime.now()
        val a = _accountCrudRepo.save(entity)
        securitySvc.onCrudEvent(a,eventType)
        return a
    }

    @Transactional
    override fun deleteById(id: String) {
        val a = findById(id).orElseThrow { IllegalArgumentException("Account not found") }
        securitySvc.checkCurrentUserAuthorized(a, Operation.DELETE)
        publisher.publishEvent(AccountEvent(a, CrudEventType.DELETED))
        _accountCrudRepo.deleteById(id)
    }

    override fun findById(id: String): Optional<Account> {
        val  aO = if( "my" == id) {
            val userId = securitySvc.getCallerInfo().currentUser.id
            val accounts = _accountCrudRepo.findByOwnerId(userId)
            if(accounts.isEmpty()) {
                Optional.empty()
            } else {
                if( accounts.size>1){
                    throw IllegalStateException("Multiple accounts found for user $userId")
                }
                Optional.of(accounts[0])
            }
        } else {
            _accountCrudRepo.findById(id)
        }
        if( aO.isPresent){
            securitySvc.checkCurrentUserAuthorized(aO.get(), READ)
        }
        return aO
    }
}
