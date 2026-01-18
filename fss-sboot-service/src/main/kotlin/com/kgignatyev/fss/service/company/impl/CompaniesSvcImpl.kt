package com.kgignatyev.fss.service.company.impl

import com.kgignatyev.fss.service.accounts.AccountsSvc
import com.kgignatyev.fss.service.common.data.SearchResult
import com.kgignatyev.fss.service.company.CompaniesService
import com.kgignatyev.fss.service.company.Company
import com.kgignatyev.fss.service.company.impl.storage.CompaniesRepo
import com.kgignatyev.fss.service.security.SecuritySvc
import jakarta.transaction.Transactional
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
@Transactional
class CompaniesSvcImpl(
    val _companiesRepo: CompaniesRepo,
    val accountsSvc: AccountsSvc,
    val securitySvc: SecuritySvc
): CompaniesService, CrudRepository<Company, String> by _companiesRepo{

    override fun search(searchExpr: String, sortExpr: String, offset: Long, limit: Int): SearchResult<Company> {
        return searchImpl(searchExpr,sortExpr, offset, limit, _companiesRepo)
    }

    override fun <S : Company?> save(company: S & Any): S & Any {
        val a = accountsSvc.findById(company.accountId).get()
        if( company.accountId == "my") {
            company.accountId = a.id
        }
        securitySvc.checkCurrentUserAuthorized(a, "update")
        company.updatedAt = OffsetDateTime.now()
        if( company.id == "") {
            company.createdAt = company.updatedAt!!
        }
        return _companiesRepo.save(company)
    }
}
