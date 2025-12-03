import { Component } from '@angular/core';
import {AuthzService} from "../../services/authz.service";

@Component({
  selector: 'app-footer',
  standalone: false,
  templateUrl: './footer.html',
})
export class Footer {

  constructor(protected authzService: AuthzService) {

  }

  protected stopImpersonation() {
    this.authzService.stopImpersonation();
    window.location.reload();
  }
}
