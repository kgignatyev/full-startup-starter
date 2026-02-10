package com.kgignatyev.fss.service.automation.impl.leads_acquisition_wf.activities_impl

import com.kgignatyev.fss.service.automation.impl.leads_acquisition_wf.LeadAcquisitionData
import com.kgignatyev.fss.service.automation.impl.leads_acquisition_wf.LeadsAcquisitionWorkflow.Companion.QUEUE_LEADS_ACQUISITION_WF
import com.kgignatyev.fss.service.automation.impl.leads_acquisition_wf.LeadsHandlingActivity
import io.temporal.spring.boot.ActivityImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
@ActivityImpl( taskQueues = [QUEUE_LEADS_ACQUISITION_WF])
class LeadsHandlingActivityImpl: LeadsHandlingActivity {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun acquireLeads(sourceId: String): LeadAcquisitionData {
        logger.warn("Simulating: Started Lead Acquisition data from $sourceId")
        val res = LeadAcquisitionData()
        res.numLeads = 10
        res.groupId = UUID.randomUUID().toString()
        return res

    }

    override fun deduplicateLeads(groupId: String): Int {
        logger.warn("Simulating: deduplication of Group $groupId")
        return 5
    }

    override fun matchAgainstSeekerPreferences(groupId: String) {
        logger.warn("Simulating: matching seekers preference for Group $groupId")
    }

}
