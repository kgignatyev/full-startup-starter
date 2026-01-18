package com.kgignatyev.fss.service.utils

import com.kgignatyev.fss.service.FssService
import com.kgignatyev.fss.service.job.Job
import com.kgignatyev.fss.service.job.impl.storage.JobsRepo
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1CompanyResponse
import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1JobStatus
import org.apache.commons.csv.CSVFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import java.io.BufferedReader
import java.io.FileReader
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.system.exitProcess


object ImportTestData {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @JvmStatic
    fun main(args: Array<String>) {
        val cxt = SpringApplication.run(FssService::class.java, *args)
        try {
            val jobsRepo = cxt.getBean(JobsRepo::class.java)
            val csvFilePath = "storage/jobs.csv"
            BufferedReader(FileReader(csvFilePath)).use { reader ->
                val csvParser = CSVFormat.EXCEL.builder()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true).setTrim(true).build()
                    .parse(reader)
                for (r in csvParser) {
                    val job = Job()
                    job.source = r.get("source") ?: "N/A"
                    job.sourceId = r.get("source_id") ?: "N/A"
                    job.status = V1JobStatus.FOR_REVIEW
                    job.title = r.get("title") ?: "N/A"
                    job.companyName = r.get("company")
                    job.companyResponse = V1CompanyResponse.NONE
                    job.createdAt = toOffsetDateTime(r.get("created_at"))
                    job.createdAt = toOffsetDateTime(r.get("updated_at"))
                    try {
                        jobsRepo.save(job)
                    } catch (e: Exception) {
                        logger.warn("Problem with row:$r", e)
                    }
                }
            }
            exitProcess(0)
        } finally {
            cxt.stop()
            exitProcess(1)
        }
    }

    private fun toOffsetDateTime(v: String?): OffsetDateTime {
        return if (v.isNullOrEmpty()) {
            OffsetDateTime.now()
        } else {
            val ldt = LocalDateTime.parse(v.replace(" ", "T"))
            ldt.atOffset(ZoneOffset.ofHours(-8))
        }
    }
}
