import {Component, ViewChild} from '@angular/core';
import {DxDataGridComponent} from "devextreme-angular";
import {
  SecurityServiceV1Service,
  V1SearchRequest,
  V1User
} from "../../generated/api_client";
import {ContextService} from "../../services/context.service";
import {Router} from "@angular/router";
import CustomStore from "devextreme/data/custom_store";
import {lastValueFrom} from "rxjs";
import DevExpress from "devextreme";
import LoadOptions = DevExpress.common.data.LoadOptions;
import {LoadResultObject} from "devextreme/common/data";

@Component({
  selector: 'app-users-list',
  standalone: false,
  templateUrl: './users-list.html',
})
export class UsersList {

  @ViewChild('usersDataGrid', {static: false}) usersDataGrid: DxDataGridComponent | undefined;
  usersDataSource: CustomStore<V1User, any>;


  constructor(private secSvc: SecurityServiceV1Service,
              private cxtSvc: ContextService,
              private router: Router) {
    const cmp = this
    this.usersDataSource = new CustomStore<V1User>({
      key: 'id',
      load: cmp.searchUsers
    });

  }

  searchUsers = (loadOptions:LoadOptions<any>) =>  {
    let searchRequest:V1SearchRequest = {
      pagination: {
        offset: 0,
        limit: 20
      }, searchExpression: "", sortExpression: "name ASC"

    } ;
    return lastValueFrom(
      this.secSvc.searchUsers( searchRequest )
    ).then(result => {
        const res: LoadResultObject = {
          data: result.items,
          totalCount: result.listSummary.total,
          summary: [],
          groupCount: 0
        }
        return res;
      }
    )
  }


  refresh() {
    this.usersDataGrid?.instance.refresh().then(d => console.info("Refreshed"))
  }
}
