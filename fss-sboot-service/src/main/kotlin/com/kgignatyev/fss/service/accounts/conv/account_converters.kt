package com.kgignatyev.fss.service.accounts.conv

import com.kgignatyev.fss.service.accounts.Account
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1Account
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component


@Component
class Account2API:Converter<Account, V1Account> {
    override fun convert(source: Account): V1Account {
        val res = V1Account()
        res.id = source.id
        res.ownerId = source.ownerId
        res.name = source.name
        res.notes = source.notes
        res.active = source.active
        res.createdAt = source.createdAt
        return res
    }
}

@Component
class API2Account:Converter<V1Account, Account> {
    override fun convert(source: V1Account): Account {
        val res = Account()
        res.id = source.id
        res.name = source.name
        res.ownerId = source.ownerId
        res.notes = source.notes
        res.active = source.active
        res.createdAt = source.createdAt
        return res
    }
}
