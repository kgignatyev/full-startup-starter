import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {CompaniesListComponent} from "./company/companies-list/companies-list.component";
import {CompanyComponent} from "./company/company/company.component";
import {JobsListComponent} from "./job/jobs-list/jobs-list.component";
import {JobComponent} from "./job/job/job.component";
import {AboutComponent} from "./system/about/about.component";
import {Workflows} from "./automation/workflows/workflows";
import {SystemInfo} from "./system/system-info/system-info";
import {UsersList} from "./system/users-list/users-list";

const routes: Routes = [

  {path: 'companies', component: CompaniesListComponent},
  {path: 'companies/:id', component: CompanyComponent},
  {path: 'jobs', component: JobsListComponent},
  {path: 'jobs/:id', component: JobComponent},
  {path: 'users', component: UsersList},
  {path: 'system-info', component: SystemInfo},
  {path: 'workflows', component: Workflows},
  {path: '', component: AboutComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
