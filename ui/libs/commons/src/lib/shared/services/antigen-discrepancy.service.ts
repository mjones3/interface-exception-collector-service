import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class AntigenDiscrepancyService {
  antigenDiscrepancyEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.antigenDiscrepancyEndpoint = config.env.serverApiURL + '/v1/antigen-discrepancies';
  }

  public getAntigenDiscrepancyByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<any>> {
    return this.httpClient
      .get<any>(this.antigenDiscrepancyEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public updateAntigenDiscrepancy(id: string | number, antigenDiscrepancy: any): Observable<HttpResponse<any>> {
    return this.httpClient
      .put<any>(`${this.antigenDiscrepancyEndpoint}/${id}`, antigenDiscrepancy, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
