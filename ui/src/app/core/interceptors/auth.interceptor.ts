import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { ExcludedUrlRegex } from 'app/shared/models';
import { AuthService } from 'app/shared/services';
import { Observable, combineLatest, throwError } from 'rxjs';
import { catchError, mergeMap } from 'rxjs/operators';
import { LoaderService } from '../loader/loader.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  /**
   * Constructor
   */
  constructor(private _router: Router, private _authService: AuthService, private _loaderService: LoaderService) {}

  /**
   * Calls to update the keycloak token if the request should update the token.
   *
   * @param req http request from @angular http module.
   * @returns
   * A promise boolean for the token update or noop result.
   */
  private async conditionallyUpdateToken(req: HttpRequest<unknown>): Promise<boolean> {
    if (this._authService.shouldUpdateToken(req)) {
      return await this._authService.updateToken();
    }

    return true;
  }

  /**
   * @deprecated
   * Checks if the url is excluded from having the Bearer Authorization
   * header added.
   *
   * @param req http request from @angular http module.
   * @param excludedUrlRegex contains the url pattern and the http methods,
   * excluded from adding the bearer at the Http Request.
   */
  private isUrlExcluded({ method, url }: HttpRequest<unknown>, { urlPattern, httpMethods }: ExcludedUrlRegex): boolean {
    const httpTest = httpMethods.length === 0 || httpMethods.join().indexOf(method.toUpperCase()) > -1;

    const urlTest = urlPattern.test(url);

    return httpTest && urlTest;
  }

  /**
   * Intercept implementation that checks if the request url matches the excludedUrls.
   * If not, adds the Authorization header to the request if the user is logged in.
   *
   * @param req
   * @param next
   */
  public intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const { enableBearerInterceptor, excludedUrls } = this._authService;
    if (!enableBearerInterceptor) {
      return next.handle(req);
    }

    const shallPass: boolean =
      !this._authService.shouldAddToken(req) || excludedUrls.findIndex(item => this.isUrlExcluded(req, item)) > -1;
    if (shallPass) {
      return next.handle(req);
    }

    return combineLatest([this.conditionallyUpdateToken(req), this._authService.isLoggedIn()]).pipe(
      mergeMap(([_, isLoggedIn]) =>
        isLoggedIn ? this.handleRequestWithTokenHeader(req, next) : this.handleRequestWithoutTokenHeader(req, next)
      )
    );
  }

  /**
   * Adds the token of the current user to the Authorization header
   *
   * @param req
   * @param next
   */
  private handleRequestWithTokenHeader(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return this._authService.addTokenToHeader(req.headers).pipe(
      mergeMap(headersWithBearer => {
        const kcReq = req.clone({ headers: headersWithBearer });
        return next.handle(kcReq).pipe(
          catchError((error: HttpErrorResponse) => {
            this.handleError(error);
            return throwError(error);
          })
        );
      })
    );
  }

  private handleRequestWithoutTokenHeader(
    req: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        this.handleError(error);
        return throwError(error);
      })
    );
  }

  private handleError(error: HttpErrorResponse): void {
    // Hide the loader
    this._loaderService.hide();

    // Handle the error
    if (error.status === 401) {
      // Sign out
      this._authService.logout();

      // Reload the app
      location.reload();
    }
    if (error.status === 403) {
      // Navigate to the 403 page
      this._router.navigate(['/errors/403']);
    }
  }
}
