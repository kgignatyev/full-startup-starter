package com.kgignatyev.fss.service.accounts

import com.kgignatyev.fss.service.common.data.SearchableRepo
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean

interface AccountsSvc: SearchableRepo<Account>,CrudRepository<Account, String>{

}
