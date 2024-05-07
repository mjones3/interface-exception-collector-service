import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DeferralCodeDto, DeferralDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<DeferralDto>;
type EntityArrayResponseType = HttpResponse<DeferralDto[]>;

@Injectable({
  providedIn: 'root',
})
export class DeferralService {
  deferralEndpoint: string;
  deferralCodesEndpoint: string;
  createDeferralEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.deferralEndpoint = config.env.serverApiURL + '/v1/deferrals';
    this.deferralCodesEndpoint = config.env.serverApiURL + '/v1/deferral-codes';
    this.createDeferralEndpoint = this.deferralEndpoint;
  }

  public getDeferralsByCriteria(criteria?: { [key: string]: any }): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DeferralDto[]>(this.deferralEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDeferralCodes(): Observable<HttpResponse<DeferralCodeDto[]>> {
    return this.httpClient
      .get<DeferralCodeDto[]>(this.deferralCodesEndpoint, {
        params: { page: '0', size: '1000', sort: 'descriptionKey,ASC' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createDeferral(dto): Observable<DeferralDto> {
    return this.httpClient.post<DeferralDto>(this.createDeferralEndpoint, dto).pipe(catchError(this.errorHandler));
  }

  public deactivate(dto: DeferralDto): Observable<DeferralDto> {
    return this.httpClient
      .put<DeferralDto>(`${this.createDeferralEndpoint}/${dto.id}`, dto)
      .pipe(catchError(this.errorHandler));
  }

  public getDeferralById(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<EntityResponseType>(`${this.deferralEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
