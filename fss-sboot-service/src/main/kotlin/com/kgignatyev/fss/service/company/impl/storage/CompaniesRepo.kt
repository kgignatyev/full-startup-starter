package com.kgignatyev.fss.service.company.impl.storage

import com.kgignatyev.fss.service.company.Company
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CompaniesRepo:CrudRepository<Company, String>,JpaSpecificationExecutor<Company> {
}
