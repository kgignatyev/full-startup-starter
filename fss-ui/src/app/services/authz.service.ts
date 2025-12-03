import {BehaviorSubject, firstValueFrom, Subscription} from "rxjs";
import {AuthService, User} from "@auth0/auth0-angular";
import {Router} from "@angular/router";
import {ContextService} from "./context.service";
import {Injectable, signal, Signal, WritableSignal} from "@angular/core";
import {SecurityServiceV1Service, V1User} from "../generated/api_client";

@Injectable({
  providedIn: 'root'
})
export class AuthzService {
  idToken$: BehaviorSubject<string|undefined> = new BehaviorSubject<string|undefined>("");
  public userAuth0$ = new BehaviorSubject<any>(null);
  // public hasAdminRole = false
  // public isAuthenticated = false;

  private tokenClaimsSub: Subscription | undefined;
  private userSub: Subscription | undefined;
  //new useful functionality in Angular
  public userSignal :WritableSignal<User|null> = signal(null)
  private userToImpersonate: V1User | null = null;


  constructor(public authService: AuthService, private router: Router,
               private secSvc: SecurityServiceV1Service,
              protected cxtSvc: ContextService) {




    // this.hasAdminRole$.subscribe(d => this.hasAdminRole = d)
    authService.error$.subscribe(e => {
      console.error("auth error:" + JSON.stringify(e))
      //@ts-ignore
      this.cxtSvc.errorAlert("Authentication error:" + e.error_description)
    })

    authService.isAuthenticated$.subscribe(v => {

      if (v) {
        this.tokenClaimsSub = authService.idTokenClaims$.subscribe(t => {
          console.info("token:" + JSON.stringify(t))
          if( t ){
            this.idToken$.next(t.__raw)
          }else {
            this.idToken$.next(undefined)
          }
        })
        this.userSub = authService.user$.subscribe(u => {
          console.info("auth0_user:" + JSON.stringify(u))
          this.userAuth0$.next(u)
          if( u ){
            this.userSignal.set( u )
          }else {
            this.userSignal.set(null)
          }
        });
      } else {
        console.info("User logged out")
        if (this.tokenClaimsSub) {
          this.tokenClaimsSub.unsubscribe()
        }
        if (this.userSub) {
          this.userSub.unsubscribe()
        }
      }
    });

  }

  isAuthenticated():boolean{
    return this.userSignal() != null;
  }

  login() {
    this.authService.loginWithRedirect();
  }

  logout() {
    this.authService.logout({
      logoutParams: {
        returnTo: location.origin
      }

    });
  }

  impersonateUserById(id:string):Promise<User> {
    this.userToImpersonate = null
    return firstValueFrom(  this.secSvc.getUserById(id) ).then( user => {
       this.userToImpersonate = user;
       return user
    })
  }

  getUserToImpersonate(): V1User|null {
    return this.userToImpersonate;
  }

  stopImpersonation() {
    this.userToImpersonate = null;
  }
}
