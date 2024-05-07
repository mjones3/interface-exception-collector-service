import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DhqDto } from '../models/dhq.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DhqService {
  dhqEndpoint: string;
  dhqCodesEndpoint: string;
  createDhqEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.dhqEndpoint = config.env.serverApiURL + '/v1/donor-history-questions';
    this.createDhqEndpoint = config.env.serverApiURL + '/v1/dhq-deferred-donor-reviews';
  }

  public getDhqById(id: number): Observable<HttpResponse<DhqDto>> {
    return this.httpClient
      .get<DhqDto>(this.dhqEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDhqByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<DhqDto[]>> {
    return this.httpClient
      .get<DhqDto[]>(this.dhqEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDhqChildrenByCriteria(
    parentId: number,
    criteria?: { [key: string]: any }
  ): Observable<HttpResponse<DhqDto[]>> {
    return this.httpClient
      .get<DhqDto[]>(`${this.dhqEndpoint}/${parentId}/children`, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getReviewDecision(): Observable<HttpResponse<DhqDto[]>> {
    return this.httpClient
      .get<DhqDto[]>(this.dhqCodesEndpoint, {
        params: { page: '0', size: '1000', sort: 'descriptionKey,ASC' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
