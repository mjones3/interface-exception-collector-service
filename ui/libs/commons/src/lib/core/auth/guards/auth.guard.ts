import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate, CanActivateChild {
  /**
   * Constructor
   *
   * @param {AuthService} _authService
   * @param {Router} _router
   */
  constructor(private _authService: AuthService, private _router: Router) {}

  // -----------------------------------------------------------------------------------------------------
  // @ Private methods
  // -----------------------------------------------------------------------------------------------------

  /**
   * CanActivate checks if the user is logged in and get the full list of roles (REALM + CLIENT)
   * of the logged user. This values are set to authenticated and roles params.
   *
   * @param route
   * @param state
   */
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    return this._check(route);
  }

  // -----------------------------------------------------------------------------------------------------
  // @ Public methods
  // -----------------------------------------------------------------------------------------------------

  /**
   * Can activate child
   *
   * @param childRoute
   * @param state
   */
  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this._check(childRoute);
  }

  /**
   * Check the authenticated status
   *
   * @private
   * @param route
   */
  private async _check(route: ActivatedRouteSnapshot): Promise<boolean> {
    // Check the authentication status
    try {
      const authenticated = await this._authService.isLoggedIn();
      if (!authenticated) {
        // Redirect to the home page
        await this._authService.login();
        // Prevent the access
        return false;
      }
      if (route.data.roles) {
        const roles = this._authService.getUserRoles();
        if (!roles) {
          // Redirect to the home page
          this._router.navigateByUrl('/');
          // Prevent the access
          return false;
        }
        if (!roles.some(role => route.data.roles.includes(role))) {
          // Redirect to the home page
          this._router.navigateByUrl('/');

          // Prevent the access
          return false;
        }
      }
      return true;
    } catch (error) {
      throw new Error('An error happened during access validation. Details:' + error);
    }
  }
}
