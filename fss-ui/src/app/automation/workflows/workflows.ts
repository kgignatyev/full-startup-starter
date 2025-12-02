import {Component, OnInit, ViewChild} from '@angular/core';
import {AutomationServiceV1Service, V1StartWorkflowRequest, V1WorkflowInfo} from "../../generated/api_client";
import {DxDataGridComponent} from "devextreme-angular";
import CustomStore from "devextreme/data/custom_store";
import {lastValueFrom} from "rxjs";

@Component({
  selector: 'app-workflows',
  standalone: false,
  templateUrl: './workflows.html'
})
export class Workflows implements OnInit {

  workflowsDataSource: CustomStore<V1WorkflowInfo[],any>;

  @ViewChild( 'workflowsDataGrid', {static: false}) workflowsDataGrid: DxDataGridComponent | undefined;

  constructor(private automationService: AutomationServiceV1Service) {
    this.workflowsDataSource = new CustomStore<V1WorkflowInfo[]> ({
      key: 'id',
      load: () => lastValueFrom(
        this.automationService.listWorkflows()
      ).then(result => result ?? [])
    });
  }

  ngOnInit(): void {
    // this.refresh();
  }

  refresh() {
    this.workflowsDataGrid?.instance.refresh();
  }

  startLeadsAcquisition() {
    let request:V1StartWorkflowRequest = {
      wfType: 'LeadsAcquisitionWorkflow'
    };
    this.automationService.startWorkflow( request).subscribe( d => this.refresh())
  }
}
