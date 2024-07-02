import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
    HttpResponse,
} from '@angular/common/http';
import { Inject, Injectable, InjectionToken } from '@angular/core';
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

/**
 * Loader http interceptor to track request to handle app loader.
 */
@Injectable()
export class LoaderInterceptorService implements HttpInterceptor {
    private requestsCounter = 0;

    constructor(
        private loaderService: LoaderService,
        @Inject(LOADER_BLACKLIST_URLS)
        private blackListUrls: LoaderBlackListUrls
    ) {}

    public intercept(
        req: HttpRequest<any>,
        next: HttpHandler
    ): Observable<HttpEvent<any>> {
        const ignoreLoader =
            req.headers.get(Headers.XIgnoreLoader) === HeaderValue.True ||
            this.isInBlackList(req.url);
        if (!ignoreLoader) {
            this.setupLoader(req);
        }
        return next.handle(req).pipe(
            tap(
                (event: HttpEvent<any>) => {
                    if (event instanceof HttpResponse && !ignoreLoader) {
                        this.decreaseRequests();
                    }
                },
                (err) => {
                    if (err instanceof HttpErrorResponse) {
                        if (!ignoreLoader) {
                            this.decreaseRequests();
                        }
                        // TODO log errors
                    }
                    return throwError(err);
                }
            )
        );
    }

    private decreaseRequests() {
        if (this.requestsCounter > 0) {
            this.requestsCounter--;
        }
        if (this.requestsCounter === 0) {
            this.loaderService.hide();
        }
    }

    private isInBlackList(url: string) {
        return (
            this.blackListUrls.urls.filter((item) => url.indexOf(item) >= 0)
                .length > 0
        );
    }

    private setupLoader(req: HttpRequest<any>): void {
        this.requestsCounter++;
        const loaderDebounceTime = req.headers.get(Headers.XLoaderDebounceTime);
        const loaderSelector = req.headers.get(Headers.XLoaderSelector);
        this.loaderService.show(atob(loaderSelector), +loaderDebounceTime);
    }
}
