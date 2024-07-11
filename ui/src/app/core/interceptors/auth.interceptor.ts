import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { ExcludedUrlRegex } from 'app/shared/models';
import { AuthService } from 'app/shared/services';
import { Observable, combineLatest, throwError } from 'rxjs';
import { catchError, mergeMap } from 'rxjs/operators';
import { LoaderService } from '../loader/loader.service';

export const authInterceptor = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const _authService = inject(AuthService);
    const _loaderService = inject(LoaderService);
    const _router = inject(Router);

    const conditionallyUpdateToken = async (req: HttpRequest<unknown>) => {
        if (_authService.shouldUpdateToken(req)) {
            return await _authService.updateToken();
        }

        return true;
    };

    const isUrlExcluded = (
        { method, url }: HttpRequest<unknown>,
        { urlPattern, httpMethods }: ExcludedUrlRegex
    ) => {
        const httpTest =
            httpMethods.length === 0 ||
            httpMethods.join().indexOf(method.toUpperCase()) > -1;

        const urlTest = urlPattern.test(url);

        return httpTest && urlTest;
    };

    const handleRequestWithTokenHeader = (
        req: HttpRequest<unknown>,
        next: HttpHandlerFn
    ) => {
        return _authService.addTokenToHeader(req.headers).pipe(
            mergeMap((headersWithBearer) => {
                const kcReq = req.clone({ headers: headersWithBearer });
                return next(kcReq).pipe(
                    catchError((error: HttpErrorResponse) => {
                        handleError(error);
                        return throwError(error);
                    })
                );
            })
        );
    };

    const handleRequestWithoutTokenHeader = (
        req: HttpRequest<unknown>,
        next: HttpHandlerFn
    ) => {
        return next(req).pipe(
            catchError((error: HttpErrorResponse) => {
                handleError(error);
                return throwError(error);
            })
        );
    };

    const handleError = (error: HttpErrorResponse) => {
        // Hide the loader
        _loaderService.hide();

        // Handle the error
        if (error.status === 401) {
            // Sign out
            _authService.logout();

            // Reload the app
            location.reload();
        }
        if (error.status === 403) {
            // Navigate to the 403 page
            _router.navigate(['/errors/403']);
        }
    };

    const { enableBearerInterceptor, excludedUrls } = _authService;
    if (!enableBearerInterceptor) {
        return next(req);
    }

    const shallPass: boolean =
        !_authService.shouldAddToken(req) ||
        excludedUrls.findIndex((item) => isUrlExcluded(req, item)) > -1;
    if (shallPass) {
        return next(req);
    }

    return combineLatest([
        conditionallyUpdateToken(req),
        _authService.isLoggedIn(),
    ]).pipe(
        mergeMap(([_, isLoggedIn]) =>
            isLoggedIn
                ? handleRequestWithTokenHeader(req, next)
                : handleRequestWithoutTokenHeader(req, next)
        )
    );
};
