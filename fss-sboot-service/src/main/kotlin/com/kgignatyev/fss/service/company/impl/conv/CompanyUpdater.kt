package com.kgignatyev.fss.service.company.impl.conv

import com.kgignatyev.fss.service.common.data.Updater
import com.kgignatyev.fss.service.company.Company
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1CompanyInfo
import org.springframework.stereotype.Component

@Component
class CompanyUpdater : Updater<V1CompanyInfo, Company> {
    override fun applyUpdate(
        source: V1CompanyInfo,
        target: Company
    ) {
        target.name = source.name
        applyNotNull(source.notes) { target.notes = it }
        applyNotNull( source.accountId) { target.accountId = it }
        applyNotNull( source.sourceId) { target.sourceId = it }
        applyNotNull( source.banned) { target.banned = it }
    }
}
