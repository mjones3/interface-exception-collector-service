import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FuseNavigationItem } from '@fuse/components/navigation';
import { Observable, ReplaySubject, tap, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MenuModel } from '../models/index';
import { EnvironmentConfigService } from './environment-config.service';

declare const dT_: any;

//Common rest
@Injectable({
    providedIn: 'root',
})
export class MenuService {
    private _menus: ReplaySubject<MenuModel[]> = new ReplaySubject<MenuModel[]>(1);

    constructor(
        private httpClient: HttpClient,
        private config: EnvironmentConfigService
    ) {
        if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
            dT_.initAngularNg(httpClient, Headers);
        }
    }

    get url(): string {
        return `${this.config.env.serverApiURL}/v1/menus`;
    }

    get menus$(): Observable<FuseNavigationItem[]> {
        return this._menus.asObservable();
    }

    // Error handlers
    public errorHandler(error: HttpErrorResponse): Observable<any> {
        return throwError(error);
    }

    get(): Observable<MenuModel[]> {
        return this.httpClient
            .get<MenuModel[]>(this.url)
            .pipe(tap((menus) => this._menus.next(menus)));
    }

    public getMenu(): Observable<MenuModel[]> {
        return this.httpClient
            .get<MenuModel[]>(this.url)
            .pipe(catchError(this.errorHandler));
    }
}
