import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {AuthService} from "@auth0/auth0-angular";
import {AuthzService} from "../../services/authz.service";

@Component({
  selector: 'app-footer',
  standalone: false,
  templateUrl: './footer.html',
})
export class Footer {

  constructor(protected authzService: AuthzService) {

  }

}
