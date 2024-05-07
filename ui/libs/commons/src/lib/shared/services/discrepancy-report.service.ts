import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DiscrepancyReportDto } from '../models';
import { ResolveDiscrepancyDto } from '../models/resolve-discrepancy.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DiscrepancyReportService {
  discrepancyEndpoint: string;
  constructor(private http: HttpClient, private config: EnvironmentConfigService) {
    this.discrepancyEndpoint = `${config.env.serverApiURL}/v1/qc-discrepancies`;
  }

  getDiscrepancyReportByCriteria(criteria: {
    [key: string]: string;
  }): Observable<HttpResponse<DiscrepancyReportDto[]>> {
    return this.http.get<DiscrepancyReportDto[]>(this.discrepancyEndpoint, {
      params: criteria,
      observe: 'response',
    });
  }

  getDiscrepancyById(id: string | number): Observable<HttpResponse<DiscrepancyReportDto>> {
    return this.http
      .get<DiscrepancyReportDto>(`${this.discrepancyEndpoint}/${id}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  getAntigenHistoricalResultByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<any>> {
    return this.http
      .get<any>(`${this.discrepancyEndpoint}/antigen/historical-results`, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  getHgbsHistoricalResultByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<any>> {
    return this.http
      .get<any>(`${this.discrepancyEndpoint}/hgbs/historical-results`, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  resolveDiscrepancy(id: number, dto: ResolveDiscrepancyDto): Observable<ResolveDiscrepancyDto> {
    return this.http
      .put<ResolveDiscrepancyDto>(`${this.discrepancyEndpoint}/${id}/resolve`, dto)
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
