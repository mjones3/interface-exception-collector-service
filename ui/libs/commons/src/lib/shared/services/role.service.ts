import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RoleDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<RoleDto>;
type EntitiesResponseType = HttpResponse<RoleDto[]>;

declare var dT_: any;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class RoleService {
  endpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
    this.endpoint = config.env.serverApiURL + '/v1/roles';
  }

  public findAll(): Observable<RoleDto[]> {
    return this.httpClient.get<RoleDto[]>(this.endpoint).pipe(catchError(this.errorHandler));
  }

  public findByCriteria(criteria?: { [key: string]: any }): Observable<EntitiesResponseType> {
    return this.httpClient
      .get<RoleDto[]>(this.endpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public findById(id: string): Observable<RoleDto> {
    return this.httpClient.get<RoleDto>(this.endpoint + '/' + id).pipe(catchError(this.errorHandler));
  }

  public update(dto: RoleDto): Observable<RoleDto> {
    return this.httpClient.put<RoleDto>(this.endpoint + '/' + dto.id, dto).pipe(catchError(this.errorHandler));
  }

  public create(dto: RoleDto): Observable<RoleDto> {
    return this.httpClient.post<RoleDto>(this.endpoint, dto).pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
