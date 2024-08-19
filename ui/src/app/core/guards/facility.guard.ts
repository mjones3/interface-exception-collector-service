import { inject } from '@angular/core';
import {
    ActivatedRouteSnapshot,
    CanActivateChildFn,
    CanActivateFn,
    Router,
    RouterStateSnapshot,
} from '@angular/router';
import { Cookie } from 'app/shared/types/cookie.enum';
import { CookieService } from 'ngx-cookie-service';

export const FacilityGuard: CanActivateFn | CanActivateChildFn = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
) => {
    const facilityCookie = inject(CookieService).get(Cookie.XFacility);
    if (state.url.indexOf('/home') >= 0) {
        return true;
    } else if (!facilityCookie) {
        inject(Router).navigate(['/home']);
        return false;
    }
    return true;
};
