package com.kgignatyev.fss.service.job.impl.conv

import com.kgignatyev.fss.service.job.JobEvent
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1JobEvent
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component


@Component
class ApiToJobEvent : Converter<V1JobEvent, JobEvent?> {
    override fun convert(s: V1JobEvent): JobEvent? {
        val j = JobEvent()
        j.id = s.id
        j.jobId = s.jobId
        j.notes = s.notes
        j.eventType = s.eventType
        j.createdAt = s.createdAt
        return j
    }
}

@Component
class JobEvent2Api : Converter<JobEvent, V1JobEvent?> {
    override fun convert(s: JobEvent): V1JobEvent? {
        val j = V1JobEvent()
        j.id = s.id
        j.jobId = s.jobId
        j.notes = s.notes
        j.eventType = s.eventType
        j.createdAt = s.createdAt
        return j
    }
}
