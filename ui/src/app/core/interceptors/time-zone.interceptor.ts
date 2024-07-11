import { HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

export const timezoneInterceptor = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const modifiedReq = req.clone({
        headers: req.headers.set(
            'X-RSA-Time-Zone',
            Intl.DateTimeFormat().resolvedOptions().timeZone
        ),
    });
    return next(modifiedReq);
};
