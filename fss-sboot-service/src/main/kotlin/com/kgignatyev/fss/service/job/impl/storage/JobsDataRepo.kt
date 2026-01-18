package com.kgignatyev.fss.service.job.impl.storage

import com.kgignatyev.fss.service.job.Job
import com.kgignatyev.fss.service.job.JobEvent
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
@Transactional
interface JobsRepo : CrudRepository<Job, String>, JpaSpecificationExecutor<Job> {
    fun findByAccountId(id: String): List<Job>
}


@Repository
@Transactional
interface JobEventsRepo : CrudRepository<JobEvent, String>, JpaSpecificationExecutor<JobEvent> {
    fun deleteByJobId(id: String)
    fun findByJobIdOrderByCreatedAtDesc(id: String): List<JobEvent>
}
