import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AntiseraEntriesDto, AntiseraEntryReportsDto } from '../models/antisera-entry-report.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<AntiseraEntryReportsDto>;
type EntityArrayResponseType = HttpResponse<AntiseraEntryReportsDto[]>;

@Injectable({
  providedIn: 'root',
})
export class AntiseraEntryReportsService {
  antiseraEntryReportsEndpoint: string;
  antiseraEntriesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.antiseraEntryReportsEndpoint = config.env.serverApiURL + '/v1/antisera-entry-report';
    this.antiseraEntriesEndpoint = config.env.serverApiURL + '/v1/qc-entries';
  }

  public getAntiseraEntryReportsByCriteria(criteria: {}): Observable<HttpResponse<EntityResponseType>> {
    return this.httpClient
      .get<EntityArrayResponseType>(this.antiseraEntryReportsEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAntiseraEntriesById(id: number): Observable<HttpResponse<AntiseraEntriesDto>> {
    return this.httpClient
      .get<AntiseraEntriesDto>(`${this.antiseraEntriesEndpoint}/${id}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
