import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HlaHpaAntigenTypeDto, HlaOrHpaEntryDto } from '../models/hla-hpa-antigen-batch.dto';
import { HlaHpaAntigenTypeEntryDTO } from '../models/hla-hpa-antigen-entry.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class HlaHpaAntigenEntriesService {
  batchEndpoint: string;
  hlaHpaAntigenTypeEndpoint = '';
  hlaHpaAntigenTypeEntryEndpoint = '';
  hlaHpaAntigenEntries: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.hlaHpaAntigenEntries = config.env.serverApiURL + '/v1/hla-hpa-antigen-entries';
    this.hlaHpaAntigenTypeEndpoint = config.env.serverApiURL + '/v1/hla-hpa-antigen-types';
    this.hlaHpaAntigenTypeEntryEndpoint = config.env.serverApiURL + '/v1/hla-hpa-antigen-type-entries';
  }

  public getByCriteria(criteria: object): Observable<HttpResponse<HlaOrHpaEntryDto[]>> {
    return this.httpClient
      .get<HlaOrHpaEntryDto[]>(this.hlaHpaAntigenEntries, {
        observe: 'response',
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public createHlaHpaEntries(testResults: HlaOrHpaEntryDto[]): Observable<HttpResponse<HlaOrHpaEntryDto[]>> {
    return this.httpClient
      .post<HlaOrHpaEntryDto[]>(`${this.hlaHpaAntigenEntries}/batch`, testResults, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  public getAntigenTypeEntry(criteria?: object): Observable<HttpResponse<HlaHpaAntigenTypeEntryDTO[]>> {
    return this.httpClient
      .get<HlaHpaAntigenTypeEntryDTO[]>(this.hlaHpaAntigenTypeEntryEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAntigenType(criteria?: object): Observable<HttpResponse<HlaHpaAntigenTypeDto[]>> {
    return this.httpClient
      .get<HlaHpaAntigenTypeDto[]>(this.hlaHpaAntigenTypeEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }
}
