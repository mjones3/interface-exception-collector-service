import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HgbsKitDto } from '../models';
import {
  HgbsEntriesDto,
  HgbsReagentsKitsListDetailsDto,
  HgbsReagentsKitsListReportDetailsDto,
  HgbsReagentsKitsListReportDto,
} from '../models/hgbs-reagents-kits-list.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class HgbsKitsService {
  hgbsKitsReportEndpoint: string;
  hgbsKitsReportReagentDetailsEndpoint: string;
  hgbsKitsEndpoint: string;
  hgbsKitEntriesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.hgbsKitsReportEndpoint = config.env.serverApiURL + '/v1/hgbs-kit-entries-report';
    this.hgbsKitsReportReagentDetailsEndpoint = config.env.serverApiURL + '/v1/hgbs-kit-entries-report/reagents';
    this.hgbsKitsEndpoint = config.env.serverApiURL + '/v1/kits';
    this.hgbsKitEntriesEndpoint = config.env.serverApiURL + '/v1/hgbs-kit-entries';
  }

  public getHgbsKitsReportByCriteria(criteria: {}): Observable<HttpResponse<HgbsReagentsKitsListReportDto>> {
    return this.httpClient
      .get<HgbsReagentsKitsListReportDto[]>(this.hgbsKitsReportEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getHgbsKitsReportReagentDetailsByCriteria(criteria: {}): Observable<
    HttpResponse<HgbsReagentsKitsListReportDetailsDto>
  > {
    return this.httpClient
      .get<HgbsReagentsKitsListReportDetailsDto[]>(this.hgbsKitsReportReagentDetailsEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getHgbsKitsById(id: number): Observable<HttpResponse<HgbsKitDto>> {
    return this.httpClient
      .get<HgbsReagentsKitsListDetailsDto[]>(`${this.hgbsKitsEndpoint}/${id}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getHgbsKitEntriesById(id: number): Observable<HttpResponse<HgbsEntriesDto>> {
    return this.httpClient
      .get<HgbsEntriesDto>(`${this.hgbsKitEntriesEndpoint}/${id}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
