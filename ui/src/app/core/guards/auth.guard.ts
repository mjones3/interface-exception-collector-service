import { inject } from '@angular/core';
import {
    ActivatedRouteSnapshot,
    CanActivateChildFn,
    CanActivateFn,
    Router,
    RouterStateSnapshot,
} from '@angular/router';
import { AuthService } from 'app/shared/services';

export const AuthGuard: CanActivateFn | CanActivateChildFn = async (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
) => {
    const _authService = inject(AuthService);
    const _router = inject(Router);

    // Check the authentication status
    try {
        const authenticated = await _authService.isLoggedIn();
        if (!authenticated) {
            // Redirect to the home page
            await _authService.login();
            // Prevent the access
            return false;
          }
        if (route.data.roles) {
            const roles = _authService.getUserRoles();
            if (!roles) {
                // Redirect to the home page
                _router.navigateByUrl('/');
                // Prevent the access
                return false;
            }
            if (!roles.some((role) => route.data.roles.includes(role))) {
                // Redirect to the home page
                _router.navigateByUrl('/');

                // Prevent the access
                return false;
            }
        }
        return true;
    } catch (error) {
        throw new Error(
            'An error happened during access validation. Details:' + error
        );
    }
};
