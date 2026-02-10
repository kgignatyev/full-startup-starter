package com.kgignatyev.fss.service.security.impl

import com.kgignatyev.fss.service.common.data.SearchResult
import com.kgignatyev.fss.service.security.User
import com.kgignatyev.fss.service.security.UserSvc
import com.kgignatyev.fss.service.security.storage.UsersRepo
import jakarta.transaction.Transactional
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
@Transactional
class UserSvcImpl( val usersRepo: UsersRepo): UserSvc, CrudRepository<User,String> by usersRepo {
    override fun findByJwtSub(realUserSub: String): Optional<User> {
        return usersRepo.findByJwtSub(realUserSub)
    }

    override fun search(searchExpr: String, sortExpr: String, offset: Long, limit: Int): SearchResult<User> {
        return searchImpl(searchExpr, sortExpr, offset, limit, usersRepo)
    }
}
