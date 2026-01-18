package com.kgignatyev.fss.service.job.impl

import com.kgignatyev.fss.service.common.data.SearchResult
import com.kgignatyev.fss.service.common.utils.DataUtils
import com.kgignatyev.fss.service.job.Job
import com.kgignatyev.fss.service.job.JobEvent
import com.kgignatyev.fss.service.job.JobEventService
import com.kgignatyev.fss.service.job.JobService
import com.kgignatyev.fss_svc.api.fsssvc.JobsServiceV1Api
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1Job
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1JobEvent
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1JobListResult
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1JobStatus
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1JobUpdateCmd
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1SearchRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["/api"])
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"], methods = [RequestMethod.PATCH, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.PUT])
@RestController
class JobsServiceV1ApiImpl(val jobsSvc: JobService,
                           val jobEventService: JobEventService,
                           @Qualifier("mvcConversionService") val conv: ConversionService
): JobsServiceV1Api {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun getAllJobs(accountId:String): ResponseEntity<List<V1Job>> {
        return ResponseEntity.ok(jobsSvc.findAll().map { conv.convert(it, V1Job::class.java)!! })
    }

    override fun searchJobs(v1SearchRequest: V1SearchRequest): ResponseEntity<V1JobListResult> {
//        logger.info("searchJobs: $v1SearchRequest")
        val r: SearchResult<Job> = jobsSvc.search(v1SearchRequest.searchExpression,v1SearchRequest.sortExpression,v1SearchRequest.pagination.offset, v1SearchRequest.pagination.limit)
        val res = V1JobListResult()
        res.items = r.items.map { conv.convert(it, V1Job::class.java)!! }
        res.listSummary = r.summary.toApiListSummary()
        return ResponseEntity.ok(res)
    }

    override fun createJob(accountId: String, v1Job: V1Job): ResponseEntity<V1Job> {
        val j = conv.convert( v1Job, Job::class.java)!!
        val r =  jobsSvc.save( j )
        return ResponseEntity.ok(conv.convert(r, V1Job::class.java))
    }

    override fun updateJobById(id: String, v1JobUpdate: V1JobUpdateCmd): ResponseEntity<V1Job> {
        val j = jobsSvc.findById(id).get()
        applyUpdate(j, v1JobUpdate)
        val r =  jobsSvc.save( j )
        return ResponseEntity.ok(conv.convert(r, V1Job::class.java))
    }

    private fun applyUpdate(
        t: Job,
        u: V1JobUpdateCmd
    ) {
        DataUtils.assignIfNotNull<Job, V1JobStatus>(t, u.status) { cv ->
            status = cv
        }
        DataUtils.assignIfNotNull<Job, String>(t, u.notes) { cv ->
            notes = cv
        }
        DataUtils.assignIfNotNull<Job, String>(t, u.sourceId) { cv ->
            sourceId = cv
        }
        DataUtils.assignIfNotNull<Job, String>(t, u.title) { cv ->
            title = cv
        }
        DataUtils.assignIfNotNull<Job, String>(t, u.companyName) { cv ->
            companyName = cv
        }

    }

    override fun createJobEvent(id: String, v1JobEvent: V1JobEvent): ResponseEntity<V1JobEvent> {
        val e = conv.convert( v1JobEvent, JobEvent::class.java)!!
        e.jobId = id
        val r =  jobEventService.save( e )
        return ResponseEntity.ok(conv.convert(r, V1JobEvent::class.java))
    }

    override fun getJobById(id: String): ResponseEntity<V1Job> {
        val v1Job = conv.convert(jobsSvc.findById(id).get(), V1Job::class.java)
        v1Job!!.events = jobEventService.findByJobId(id).map { conv.convert(it, V1JobEvent::class.java)!! }
        return ResponseEntity.ok(v1Job)
    }
}
