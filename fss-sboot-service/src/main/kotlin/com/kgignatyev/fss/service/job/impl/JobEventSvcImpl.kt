package com.kgignatyev.fss.service.job.impl

import com.kgignatyev.fss.service.common.data.SearchResult
import com.kgignatyev.fss.service.common.events.CrudEventType
import com.kgignatyev.fss.service.job.JobEvent
import com.kgignatyev.fss.service.job.JobEventEvent
import com.kgignatyev.fss.service.job.JobEventService
import com.kgignatyev.fss.service.job.impl.storage.JobEventsRepo
import com.kgignatyev.fss.service.job.impl.storage.JobsRepo
import com.kgignatyev.fss.service.security.SecuritySvc
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
@Transactional
class JobEventSvcImpl(val jobEventsRepo: JobEventsRepo,
                      val jobRepo: JobsRepo,
                      val securitySvc: SecuritySvc,
                      val publisher: ApplicationEventPublisher): JobEventService, CrudRepository<JobEvent, String> by jobEventsRepo {
    override fun deleteByJobId(id: String) {
        jobEventsRepo.deleteByJobId(id)
    }

    override fun findByJobId(id: String): List<JobEvent> {
        return jobEventsRepo.findByJobIdOrderByCreatedAtDesc(id)
    }

    override fun search(searchExpr: String, sortExpr: String, offset: Long, limit: Int): SearchResult<JobEvent> {
        return searchImpl(searchExpr, sortExpr, offset, limit, jobEventsRepo)
    }

    @Transactional
    override fun <S : JobEvent> save(entity: S ): S  {
        val job = jobRepo.findById(entity.jobId).orElseThrow { IllegalArgumentException("Job not found") }
        //todo: secure operation
        //securitySvc.checkCurrentUserAuthorized(job, CrudEventType.UPDATE)
        val eventType = if( entity.id == "") CrudEventType.CREATED else CrudEventType.UPDATED
        val evt = jobEventsRepo.save(entity)
        publisher.publishEvent(JobEventEvent(evt, eventType))
        return evt
    }
}
