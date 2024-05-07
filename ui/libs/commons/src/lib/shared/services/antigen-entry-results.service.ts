import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AntigenEntryResultDto, AntigenInactivateEntryResultDto, AntigenOverrideWeakPositiveDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class AntigenEntryResultsService {
  antigenEntryResultsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.antigenEntryResultsEndpoint = config.env.serverApiURL + '/v1/antigen-entry-results';
  }

  public getAntigenEntryResultsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<AntigenEntryResultDto[]>> {
    return this.httpClient
      .get<AntigenEntryResultDto[]>(`${this.antigenEntryResultsEndpoint}`, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAntigenEntryResultsById(id: string): Observable<HttpResponse<AntigenEntryResultDto>> {
    return this.httpClient
      .get<AntigenEntryResultDto>(`${this.antigenEntryResultsEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createAntigenEntryResult(antigenEntry: any): Observable<HttpResponse<AntigenEntryResultDto>> {
    return this.httpClient
      .post<AntigenEntryResultDto>(this.antigenEntryResultsEndpoint, antigenEntry, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public overrideWeakPositive(id: string, payload: AntigenOverrideWeakPositiveDto): Observable<AntigenEntryResultDto> {
    return this.httpClient
      .put<AntigenEntryResultDto>(`${this.antigenEntryResultsEndpoint}/${id}/override-weak-positive`, payload)
      .pipe(catchError(this.errorHandler));
  }

  public inactivateAntigen(id: string, payload: AntigenInactivateEntryResultDto): Observable<AntigenEntryResultDto> {
    return this.httpClient
      .put<AntigenEntryResultDto>(`${this.antigenEntryResultsEndpoint}/${id}/inactivate`, payload)
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
