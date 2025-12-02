import {Component, OnInit, ViewChild} from '@angular/core';
import {CompaniesServiceV1Service, V1Company} from "../../generated/api_client";
import {Router} from "@angular/router";
import {ContextService} from "../../services/context.service";
import CustomStore from "devextreme/data/custom_store";
import {lastValueFrom} from "rxjs";
import {DxDataGridComponent} from "devextreme-angular";

@Component({
  selector: 'app-companies-list',
  templateUrl: './companies-list.component.html',
  standalone: false
})
export class CompaniesListComponent {

  pageSize: number = 15;
  companiesDataSource: CustomStore<V1Company, any>;

  @ViewChild('companiesDataGrid', {static: false}) companiesDataGrid: DxDataGridComponent | undefined;


  constructor(private companiesSvc: CompaniesServiceV1Service,
              private cxtSvc: ContextService,
              private router: Router) {

    this.companiesDataSource = new CustomStore<V1Company>({
      key: 'id',
      load: () => lastValueFrom(
        this.companiesSvc.getAllCompanies(this.cxtSvc.currentAccount$.getValue())
      ).then(result => result ?? [])
    });

  }



  refresh() {
    console.info('Refresh-CompaniesListComponent');
    this.companiesDataGrid?.instance.refresh().then(d => console.info("Refreshed"))
  }

  newCompany() {
    this.router.navigate(['/companies/new']);
  }
}
