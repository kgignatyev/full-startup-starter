package com.kgignatyev.fss.service.automation.impl

import com.kgignatyev.fss.service.automation.AutomationSvc
import com.kgignatyev.fss.service.automation.WorkflowInfo
import com.kgignatyev.fss.service.automation.impl.leads_acquisition_wf.LeadsAcquisitionWorkflow
import com.kgignatyev.fss.service.common.utils.toOffsetDateTime
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class AutomationSvcImpl: AutomationSvc {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired(required = false)
    lateinit var workflowClient: WorkflowClient

    override fun startWorkflow(wfType: String) {
        if( ! ::workflowClient.isInitialized  ) {
            throw Exception("Workflow client must be initialized before starting workflow." +
                    "Please check  'temporal.connection.target' property")
        }

        when( wfType) {
            "LeadsAcquisitionWorkflow" -> startLeadAcquisitionWorkflow()
            else -> throw Exception("Unknown workflow type: $wfType")
        }
    }

    private fun startLeadAcquisitionWorkflow() {
        val newWorkflowStub = workflowClient.newWorkflowStub(LeadsAcquisitionWorkflow::class.java,
            WorkflowOptions.newBuilder()
                //forcing wf id to prevent concurrent execution of workflows
                .setWorkflowId("lead-acquisition")
                .setTaskQueue(LeadsAcquisitionWorkflow.QUEUE_LEADS_ACQUISITION_WF)
                .build())
        WorkflowClient.start( newWorkflowStub::acquireLeads, "linkedIn" )
    }

    override fun listWorkflows(): List<WorkflowInfo> {
        if( ! ::workflowClient.isInitialized  ) {
            return emptyList<WorkflowInfo>()
        }
        val request = ListWorkflowExecutionsRequest.newBuilder()
            .setNamespace("default")
            .build()

        val response = workflowClient.workflowServiceStubs
            .blockingStub()
            .listWorkflowExecutions(request)

        return response.executionsList.map { execution ->
            WorkflowInfo(
                execution.execution.workflowId,
                execution.execution.runId,
                execution.type.name,
                execution.status.name,
                execution.startTime.toOffsetDateTime(),
                execution.executionTime.toOffsetDateTime()
            )
        }
    }
}
