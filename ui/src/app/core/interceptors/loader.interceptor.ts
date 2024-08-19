import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandlerFn,
    HttpRequest,
    HttpResponse,
} from '@angular/common/http';
import { InjectionToken, inject } from '@angular/core';
import { HeaderValue } from 'app/shared/types/header-value.enum';
import { Headers } from 'app/shared/types/headers.enum';
import { Observable, throwError } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoaderService } from '../loader/loader.service';

export interface LoaderBlackListUrls {
    urls: string[];
}

export function blacklistFn() {
    //Black list any url that loader is not needed
    return { urls: ['fonts/', 'i18n/', 'icons/', 'images/', 'styles/'] };
}

export const LOADER_BLACKLIST_URLS = new InjectionToken<LoaderBlackListUrls>(
    'Loader Blacklist Urls',
    {
        providedIn: 'root',
        factory: blacklistFn,
    }
);

export const loaderInterceptor = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const loaderService = inject(LoaderService);
    const blackListUrls = inject(LOADER_BLACKLIST_URLS);

    let requestsCounter = 0;

    const decreaseRequests = () => {
        if (requestsCounter > 0) {
            requestsCounter--;
        }
        if (requestsCounter === 0) {
            loaderService.hide();
        }
    };

    const isInBlackList = (url: string) => {
        return (
            blackListUrls.urls.filter((item) => url.indexOf(item) >= 0).length >
            0
        );
    };

    const setupLoader = (req: HttpRequest<any>) => {
        requestsCounter++;
        const loaderDebounceTime = req.headers.get(Headers.XLoaderDebounceTime);
        const loaderSelector = req.headers.get(Headers.XLoaderSelector);
        loaderService.show(atob(loaderSelector), +loaderDebounceTime);
    };

    const ignoreLoader =
        req.headers.get(Headers.XIgnoreLoader) === HeaderValue.True ||
        isInBlackList(req.url);
    if (!ignoreLoader) {
        setupLoader(req);
    }
    return next(req).pipe(
        tap(
            (event: HttpEvent<any>) => {
                if (event instanceof HttpResponse && !ignoreLoader) {
                    decreaseRequests();
                }
            },
            (err) => {
                if (err instanceof HttpErrorResponse) {
                    if (!ignoreLoader) {
                        decreaseRequests();
                    }
                    // TODO log errors
                }
                return throwError(err);
            }
        )
    );
};
