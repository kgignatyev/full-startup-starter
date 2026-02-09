package com.kgignatyev.fss.service.automation.impl.leads_acquisition_wf

import com.kgignatyev.fss.service.automation.impl.common.EventType
import com.kgignatyev.fss.service.automation.impl.common.NotificationActivity
import com.kgignatyev.fss.service.automation.impl.common.NotificationActivity.Companion.QUEUE_NOTIFICATIONS
import com.kgignatyev.fss.service.automation.impl.leads_acquisition_wf.LeadsAcquisitionWorkflow.Companion.QUEUE_LEADS_ACQUISITION_WF
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityOptions
import io.temporal.spring.boot.WorkflowImpl
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import java.time.Duration

@WorkflowInterface
interface LeadsAcquisitionWorkflow {

    companion object {
        const val QUEUE_LEADS_ACQUISITION_WF = "leads_acquisition"
    }

    @WorkflowMethod
    fun acquireLeads(sourceId: String)
}

 class LeadAcquisitionData( var numLeads:Int =0, var groupId:String = "undefined")

@ActivityInterface
interface LeadsHandlingActivity {

    fun acquireLeads(sourceId: String): LeadAcquisitionData
    fun deduplicateLeads(groupId: String): Int
    fun matchAgainstSeekerPreferences(groupId: String)

}

@WorkflowImpl( taskQueues = [QUEUE_LEADS_ACQUISITION_WF])
class LeadsAcquisitionWorkflowImpl : LeadsAcquisitionWorkflow {

    val leadsHandler: LeadsHandlingActivity = Workflow.newActivityStub(
        LeadsHandlingActivity::class.java,
        ActivityOptions.newBuilder().setTaskQueue(QUEUE_LEADS_ACQUISITION_WF)
            .setStartToCloseTimeout(Duration.ofSeconds(30) )
            .build()
    )

    val notificationActivity: NotificationActivity = Workflow.newActivityStub(
        NotificationActivity::class.java,
        ActivityOptions.newBuilder().setTaskQueue(QUEUE_NOTIFICATIONS)
            .setStartToCloseTimeout(Duration.ofSeconds(3) )
            .build(),
    )

    override fun acquireLeads(sourceId: String) {
        val logger = Workflow.getLogger(LeadsAcquisitionWorkflow::class.java)
        try {
            val leadsInfo = leadsHandler.acquireLeads(sourceId)
            leadsHandler.deduplicateLeads(leadsInfo.groupId)
            leadsHandler.matchAgainstSeekerPreferences(leadsInfo.groupId)
            notificationActivity.notifyAboutEvent(
                EventType.LEAD_ACQUISITION_SUCCESS,
                "Acquired ${leadsInfo.numLeads} leads in group ${leadsInfo.groupId} from $sourceId"
            )
        } catch (e: Exception) {
            val message = "Failed getting leads from $sourceId, ${e.message}"
            logger.error(message, e)
            notificationActivity.notifyAboutEvent(EventType.LEAD_ACQUISITION_FAILURE, message)
        }
    }

}
