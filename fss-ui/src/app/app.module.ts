import { NgModule, inject, provideAppInitializer } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {fssSvcApiModule, fssSvcConfiguration} from "./generated/api_client";
import {
  DxAutocompleteModule,
  DxBoxModule,
  DxButtonModule,
  DxCalendarModule,
  DxChartModule,
  DxCheckBoxModule, DxDataGridModule,
  DxDateBoxModule,
  DxDropDownBoxModule,
  DxDropDownButtonModule,
  DxFormModule,
  DxListModule,
  DxLoadIndicatorModule,
  DxLoadPanelModule,
  DxLookupModule,
  DxNumberBoxModule,
  DxPopoverModule,
  DxPopupModule,
  DxScrollViewModule,
  DxSelectBoxModule,
  DxSwitchModule,
  DxTabPanelModule,
  DxTabsModule,
  DxTagBoxModule,
  DxTemplateModule,
  DxTextAreaModule,
  DxTextBoxModule,
  DxToastModule,
  DxTooltipModule
} from "devextreme-angular";
import { HTTP_INTERCEPTORS, HttpBackend, HttpClient, provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {CompaniesListComponent} from "./company/companies-list/companies-list.component";
import { CompanyComponent } from './company/company/company.component';
import { JobsListComponent } from './job/jobs-list/jobs-list.component';
import { JobComponent } from './job/job/job.component';
import {ContextService} from "./services/context.service";
import {CidAndJWTInterceptor, ErrorCatchingInterceptor} from "./services/interceptors";
import {AuthClientConfig, AuthConfig, AuthHttpInterceptor, AuthModule} from "@auth0/auth0-angular";
import {AuthzService} from "./services/authz.service";
import {firstValueFrom} from "rxjs";
import { AlertsComponent } from './layout/alerts/alerts.component';
import { AboutComponent } from './system/about/about.component';
import { JobSourcesDropDownComponent } from './components/job-sources-drop-down/job-sources-drop-down.component';
import { Workflows } from './automation/workflows/workflows';
import { SystemInfo } from './system/system-info/system-info';
import { Footer } from './layout/footer/footer';
import { UsersList } from './system/users-list/users-list';
@NgModule({ declarations: [
        AppComponent,
        CompaniesListComponent,
        CompanyComponent,
        JobsListComponent,
        JobComponent,
        AlertsComponent,
        AboutComponent,
        JobSourcesDropDownComponent,
        Workflows,
        SystemInfo,
        Footer,
        UsersList
    ],
    bootstrap: [AppComponent], imports: [BrowserModule,
        DxAutocompleteModule,
        DxChartModule,
        DxPopupModule,
        DxPopoverModule,
        DxSwitchModule,
        DxButtonModule,
        DxBoxModule,
        DxNumberBoxModule,
        DxTabsModule,
        DxTemplateModule,
        DxTextBoxModule,
        DxSelectBoxModule,
        DxCheckBoxModule,
        DxLoadPanelModule,
        DxListModule,
        DxLookupModule,
        DxFormModule,
        DxDropDownBoxModule,
        DxLoadIndicatorModule,
        DxDateBoxModule,
        DxDropDownButtonModule,
        DxCheckBoxModule,
        DxTabPanelModule,
        DxTagBoxModule,
        DxToastModule,
        DxScrollViewModule,
        DxCalendarModule,
        DxTextAreaModule,
        AppRoutingModule,
        fssSvcApiModule,
        FormsModule,
        DxDataGridModule,
        AuthModule.forRoot()], providers: [
        {
            provide: fssSvcConfiguration,
            useFactory: () => new fssSvcConfiguration({
                basePath: "/fss-svc/api",
            }),
            deps: [],
            multi: false
        },
        provideAppInitializer(() => {
        const initializerFn = (loadConfig)(inject(HttpBackend), inject(AuthClientConfig));
        return initializerFn();
      }),
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthHttpInterceptor,
            multi: true,
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: CidAndJWTInterceptor,
            multi: true,
            deps: [AuthzService]
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: ErrorCatchingInterceptor,
            multi: true,
            deps: [ContextService]
        },
        provideHttpClient(withInterceptorsFromDi()),
    ] })
export class AppModule { }

function loadConfig(handler: HttpBackend, auth0config: AuthClientConfig) {

  // note: this could be a remote url, for example service endpoint can
  // return the config
  let url = "/assets/config.json";


  return () => firstValueFrom(new HttpClient(handler).get(url)).then(
    (data: any) => {
      console.info("got:" + JSON.stringify(data))

      const cfg: AuthConfig = {

        authorizationParams: {
          audience: data.auth0ApiAudience,
          redirect_uri: window.location.origin,
          scope: 'openid profile email',
        },
        domain: data.auth0Domain,
        clientId: data.auth0ClientId,
        httpInterceptor: {
          allowedList: [
            '/api/*',
            '/fss-svc/api/*',
          ]
        }
      }
      auth0config.set(cfg)
    }
  );
}
