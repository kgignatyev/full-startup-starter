package com.kgignatyev.fss.service.job.impl

import com.kgignatyev.fss.service.automation.JobActivities
import com.kgignatyev.fss.service.job.JobEvent
import com.kgignatyev.fss.service.job.JobEventService
import com.kgignatyev.fss.service.job.JobService
import com.kgignatyev.fss.service.security.SecurityUtils
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1JobEventType
import io.temporal.spring.boot.ActivityImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ActivityImpl( taskQueues = ["JOBS_WORKFLOW"])
class JobActivitiesImpl(private val jobService: JobService, private val jobEventService: JobEventService): JobActivities {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    override fun recordCompanyResponse(
        jobId: String,
        responseCode: String,
        responseMessage: String
    ) {
        SecurityUtils.doAsAdmin {
            val je = JobEvent()
            je.jobId = jobId
            je.notes = responseMessage
            je.eventType = V1JobEventType.fromValue(responseCode)
            jobEventService.save(je)
        }
    }

}
