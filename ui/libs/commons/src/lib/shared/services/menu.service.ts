import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MenuDto } from '../models/index';
import { EnvironmentConfigService } from './environment-config.service';
import { MENUS } from './mocks/menus-mock';

declare var dT_: any;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class MenuService {
  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
  }

  get url(): string {
    return `${this.config.env.serverApiURL}/v1/menus`;
  }

  public getMenu(processUUID: string): Observable<MenuDto[]> {
    return of(MENUS);
    /*
    return this.httpClient
      .get<MenuDto[]>(this.url, { params: { client: processUUID } })
      .pipe(catchError(this.errorHandler));
     */
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
