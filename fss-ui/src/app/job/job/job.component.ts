import {
  AfterViewInit, ChangeDetectorRef,
  Component,
  DestroyRef,
  inject,
  NgZone,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {ContextService} from "../../services/context.service";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthzService} from "../../services/authz.service";
import {combineLatest, distinctUntilChanged, filter, ignoreElements, map, of, Subscription, switchMap, tap} from "rxjs";
import {JobsServiceV1Service, V1Job, V1JobEvent, V1JobUpdateCmd} from "../../generated/api_client";
import {SimpleItem, SimpleItemTemplateData} from "devextreme/ui/form";
import DevExpress from "devextreme";
import {DxElement} from "devextreme/core/element";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {ArrayStore} from "devextreme/common/data";

@Component({
  selector: 'app-job',
  templateUrl: './job.component.html',
  standalone: false
})
export class JobComponent implements OnDestroy, OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly zone = inject(NgZone);
  private sub!: Subscription;
  public id: string | undefined;
  public job: V1Job | undefined;
  public jobEvents: V1JobEvent[] = [];

  eventsGridDataSource: ArrayStore;

  @ViewChild('jobForm', {static: false}) jobForm!: DevExpress.ui.dxForm;
  @ViewChild('eventsGrid', {static: false}) eventsGrid!: DevExpress.ui.dxDataGrid;

  customizeJobFormItem = (item: SimpleItem) => {
    if (item.dataField == 'title') {
      item.validationRules = [
        {type: 'required', message: 'Title is required'},
        {type: 'stringLength', max: 100, message: 'Title should be less than 100 characters'},
        {type: 'stringLength', min: 5, message: 'Title should be at least 5 characters'}
      ]
    }

    if (item.dataField == 'id' || item.dataField == 'accountId') {
      item.editorOptions = {readOnly: true}
    }

    if (item.dataField == 'events') {
      item.visible = false;
    }

    if (item.dataField == 'sourceId') {
      item.template = 'jobSourceTemplate'
    }

  }

  constructor(private authzSvc: AuthzService,
              private jobSvc: JobsServiceV1Service,
              private route: ActivatedRoute,
              private router: Router,
              private cxtSvc: ContextService,
              private cdr: ChangeDetectorRef) {
    this.eventsGridDataSource = new ArrayStore({
      key: 'id',
      data: []
    });

  }

  private applyJob(job: V1Job): void {
    this.job = job;
    this.jobEvents = this.job.events || [];
    this.cdr.detectChanges();
  }

  ngOnInit(): void {
    const id$ = this.route.paramMap.pipe(
      map((params) => params.get('id') || 'no-id-param'),
      distinctUntilChanged(),
      tap((id) => (this.id = id))
    );

    const authReady$ = this.authzSvc.userAuth0$.pipe(filter((u) => !!u));
    const myComponent = this;
    // combineLatest([id$, authReady$])
    combineLatest([id$])
      .pipe(
        switchMap(([id]) => {
          if (id == 'new') {
            const job: V1Job = {
              accountId: this.cxtSvc.currentAccount$.getValue(),
              notes: '',
              sourceId: '',
              id: '',
              title: '',
              status: 'APPLIED',
              createdAt: new Date().toISOString(),
              events: []
            };
            return of(job);
          }

          return this.jobSvc.getJobById(id);
        }),
        tap({
          next: (job: V1Job) => {
            console.info("got a job", job);
            this.applyJob(job);
          },
          error: (err) => {
            console.error("tap error", err);
          },
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe();
  }


  ngOnDestroy(): void {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  cancel() {
    this.router.navigate(['/jobs']);
  }

  saveJob() {
    // @ts-ignore
    const validationResults = this.jobForm.instance.validate()
    const jobUpdateCmd: V1JobUpdateCmd = {
      companyName: this.job!!.companyName,
      notes: this.job!!.notes,
      sourceId: this.job!!.sourceId,
      companyResponse: this.job!!.companyResponse,
    }
    if (validationResults.isValid) {
      if (this.job?.id == '') {
        this.jobSvc.createJob("my", this.job).subscribe( j=> this.applyJob(j) );
      } else {
        this.jobSvc.updateJobById(this.id!!, jobUpdateCmd).subscribe(j => this.applyJob(j));
      }
    }
  }

  protected readonly JSON = JSON;
}
