import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EmployeeDto, RoleDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<EmployeeDto>;
type EntitiesResponseType = HttpResponse<EmployeeDto[]>;

declare var dT_: any;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  employeeEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
    this.employeeEndpoint = config.env.serverApiURL + '/v1/employees';
  }

  public getEmployeeById(id: string): Observable<EntityResponseType> {
    return this.httpClient
      .get<EmployeeDto>(this.employeeEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public findById(id: string): Observable<EmployeeDto> {
    return this.httpClient.get<EmployeeDto>(this.employeeEndpoint + '/' + id).pipe(catchError(this.errorHandler));
  }

  public getEmployeeByIds(ids: string[]): Observable<EntitiesResponseType> {
    const idsIn: string = ids.join(',');
    return this.httpClient
      .get<EmployeeDto[]>(`${this.employeeEndpoint}?id.in=${idsIn}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getEmployeeByCriteria(criteria?: { [key: string]: any }): Observable<EmployeeDto[]> {
    return this.httpClient
      .get<EmployeeDto[]>(this.employeeEndpoint, { params: { ...criteria } })
      .pipe(catchError(this.errorHandler));
  }

  public findEmployees(params?: { [key: string]: any }): Observable<EntitiesResponseType> {
    return this.httpClient
      .get<EmployeeDto[]>(this.employeeEndpoint, { params, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateRoles(employeeId: string, roleIds: string[]): Observable<EmployeeDto> {
    return this.httpClient
      .put(this.employeeEndpoint + '/' + employeeId + '/roles', roleIds)
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
