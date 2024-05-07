import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AntigenBatchDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class AntigenBatchService {
  batchEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.batchEndpoint = config.env.serverApiURL + '/v1/antigen-batches';
  }

  public getByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<AntigenBatchDto[]>> {
    return this.httpClient
      .get<AntigenBatchDto[]>(this.batchEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public create(antigenBatchDto: AntigenBatchDto): Observable<AntigenBatchDto> {
    return this.httpClient
      .post<AntigenBatchDto>(`${this.batchEndpoint}`, antigenBatchDto)
      .pipe(catchError(this.errorHandler));
  }

  public update(id: number, antigenBatchDto: Partial<AntigenBatchDto>): Observable<AntigenBatchDto> {
    return this.httpClient
      .put<AntigenBatchDto>(`${this.batchEndpoint}/${id}`, antigenBatchDto)
      .pipe(catchError(this.errorHandler));
  }

  public delete(id: number, antigenBatchDto: Partial<AntigenBatchDto>): Observable<AntigenBatchDto> {
    return this.httpClient.delete<AntigenBatchDto>(`${this.batchEndpoint}/${id}`).pipe(catchError(this.errorHandler));
  }

  public getAntigenBatchesById(antigenBatchId: string | number): Observable<HttpResponse<AntigenBatchDto>> {
    return this.httpClient
      .get<AntigenBatchDto>(`${this.batchEndpoint}/${antigenBatchId}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
