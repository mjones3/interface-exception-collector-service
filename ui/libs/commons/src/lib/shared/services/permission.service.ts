import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PermissionDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<PermissionDto>;
type EntitiesResponseType = HttpResponse<PermissionDto[]>;

declare var dT_: any;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  endpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
    this.endpoint = config.env.serverApiURL + '/v1/permissions';
  }

  public findAll(): Observable<PermissionDto[]> {
    return this.httpClient.get<PermissionDto[]>(this.endpoint).pipe(catchError(this.errorHandler));
  }

  public findByCriteria(criteria?: { [key: string]: any }): Observable<EntitiesResponseType> {
    return this.httpClient
      .get<PermissionDto[]>(this.endpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public findById(id: string): Observable<PermissionDto> {
    return this.httpClient.get<PermissionDto>(this.endpoint + '/' + id).pipe(catchError(this.errorHandler));
  }

  public getPermissionByCriteria(criteria?: { [key: string]: any }): Observable<PermissionDto[]> {
    return this.httpClient
      .get<PermissionDto[]>(this.endpoint, { params: { ...criteria } })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
