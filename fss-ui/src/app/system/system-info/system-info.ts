import {Component, OnInit, ViewChild} from '@angular/core';
import {SecurityServiceV1Service, V1SecurityPolicy} from "../../generated/api_client";
import {AuthzService} from "../../services/authz.service";
import {DxDataGridComponent} from "devextreme-angular";
import CustomStore from "devextreme/data/custom_store";
import {lastValueFrom} from "rxjs";

@Component({
  selector: 'app-system-info',
  standalone: false,
  templateUrl: './system-info.html'
})
export class SystemInfo implements OnInit {

  policiesDataSource:CustomStore<V1SecurityPolicy>

  @ViewChild('policiesDataGrid', {static: false}) policiesDataGrid: DxDataGridComponent | undefined;
  constructor(private securitySvc: SecurityServiceV1Service, private authz: AuthzService) {
     this.policiesDataSource = new CustomStore<V1SecurityPolicy>({
       key: 'id',
       load: () => lastValueFrom(
         this.securitySvc.getSecurityPoliciesForUser( "me")
       ).then(result => result ?? [])
     })
  }

  ngOnInit(): void {
    this.refresh()
  }

  protected refresh() {
    this.policiesDataGrid?.instance.refresh()
  }
}
