import { HttpClient } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HlaHpaAntigenBatchDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class HlaHpaAntigenBatchService {
  batchEndpoint: string;
  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.batchEndpoint = config.env.serverApiURL + '/v1/hla-hpa-antigen-batches';
  }
  public getByCriteria(criteria?: object): Observable<HttpResponse<HlaHpaAntigenBatchDto[]>> {
    return this.httpClient
      .get<HlaHpaAntigenBatchDto[]>(this.batchEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public create(hlaHpaAntigenBatchDto: HlaHpaAntigenBatchDto): Observable<HlaHpaAntigenBatchDto> {
    return this.httpClient
      .post<HlaHpaAntigenBatchDto>(`${this.batchEndpoint}`, hlaHpaAntigenBatchDto)
      .pipe(catchError(this.errorHandler));
  }

  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
