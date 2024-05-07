import { Directive, HostBinding, Inject, Input, OnInit } from '@angular/core';
import { AUTH_SERVICE_TOKEN } from '../../core/auth/auth.service';
import { AuthHasUserRole } from '../../core/auth/interfaces/auth-options';

@Directive({
  selector: '[rsaPermissionsOnly]',
  exportAs: 'rsaPermissionsOnly',
})
export class PermissionsOnlyDirective implements OnInit {
  @Input() rsaPermissionsOnly: string[];

  private _disabled = false;

  constructor(@Inject(AUTH_SERVICE_TOKEN) private authService: AuthHasUserRole) {}

  ngOnInit(): void {
    this._disabled = !this.authService.hasUserRoles(this.rsaPermissionsOnly);
  }

  @HostBinding('disabled')
  get isDisabled() {
    return this._disabled;
  }
}
