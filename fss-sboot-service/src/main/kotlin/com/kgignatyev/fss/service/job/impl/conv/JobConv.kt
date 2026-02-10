package com.kgignatyev.fss.service.job.impl.conv

import com.kgignatyev.fss.service.job.Job
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1Job
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class ApiToJob : Converter<V1Job, Job> {
    override fun convert(s: V1Job): Job {
        val j = Job()
        j.id = s.id
        j.sourceId = s.sourceId
        j.accountId = s.accountId
        j.title = s.title
        j.status = s.status
        j.notes = s.notes
        val companyName = s.companyName
        if( companyName != null )
            j.companyName = companyName
        val companyResponse = s.companyResponse
        if( companyResponse != null )
          j.companyResponse = companyResponse
        j.createdAt = s.createdAt
        val updatedAt = s.updatedAt
        if( updatedAt != null) {
            j.updatedAt = updatedAt
        }
        return j
    }

}


@Component
class Job2Api : Converter<Job, V1Job?> {
    override fun convert(s: Job): V1Job? {
        val j = V1Job()
        j.id = s.id
        j.accountId = s.accountId
        j.sourceId = s.sourceId
        j.title = s.title
        j.status = s.status
        j.notes = s.notes
        j.companyName = s.companyName
        j.companyResponse = s.companyResponse
        j.createdAt = s.createdAt
        j.updatedAt = s.updatedAt
        return j
    }

}
