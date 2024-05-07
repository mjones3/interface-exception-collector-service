import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  LookbackBloodComponentsProductsDto,
  LookbackDetailDto,
  LookbackDto,
  LookbackReportDto,
  LookbackReviewDto,
  LookbackTriggerCriteriaDto,
} from '../models';
import { LookbackConsequenceDto } from '../models/lookback-consequence.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class LookbackService {
  lookbackEndpoint: string;
  lookbackReportEndpoint: string;
  lookbackReviewEndpoint: string;
  lookbackBloodComponentsProductsEndpoint: string;
  lookbackTriggerCriteriaEndpoint: string;
  lookbackConsequencesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.lookbackEndpoint = config.env.serverApiURL + '/v1/lookbacks';
    this.lookbackReportEndpoint = config.env.serverApiURL + '/v1/lookback-review-reports';
    this.lookbackReviewEndpoint = config.env.serverApiURL + '/v1/lookback-review';
    this.lookbackConsequencesEndpoint = config.env.serverApiURL + '/v1/lookback-consequences';
    this.lookbackBloodComponentsProductsEndpoint = config.env.serverApiURL + '/v1/lookbacks/blood-components-products';
    this.lookbackTriggerCriteriaEndpoint = config.env.serverApiURL + '/v1/lookback-trigger-criterias';
  }

  public getLookbackById(id: string | number): Observable<HttpResponse<LookbackDto>> {
    return this.httpClient
      .get<LookbackDto>(`${this.lookbackEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getLookbackDetailById(id: string | number): Observable<HttpResponse<LookbackDetailDto>> {
    return this.httpClient
      .get<LookbackDetailDto>(`${this.lookbackEndpoint}/${id}/detail`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getLookbackReportByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<LookbackReportDto[]>> {
    return this.httpClient
      .get<any[]>(`${this.lookbackReportEndpoint}`, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getLookbackReviewByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<LookbackReviewDto[]>> {
    return this.httpClient
      .get<LookbackReviewDto[]>(`${this.lookbackReviewEndpoint}`, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getLookbackConsequenceByLookbackId(id: number): Observable<HttpResponse<LookbackConsequenceDto>> {
    return this.httpClient
      .get<LookbackConsequenceDto>(`${this.lookbackEndpoint}/${id}/consequence`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getLookbackConsequencesByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<LookbackConsequenceDto[]>> {
    return this.httpClient
      .get<LookbackConsequenceDto[]>(`${this.lookbackConsequencesEndpoint}`, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getLookbackBloodComponentsProducts(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<LookbackBloodComponentsProductsDto[]>> {
    return this.httpClient
      .get<LookbackBloodComponentsProductsDto[]>(`${this.lookbackBloodComponentsProductsEndpoint}`, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createLookbackReview(lookbackReview: Partial<LookbackReviewDto>): Observable<HttpResponse<LookbackReviewDto>> {
    return this.httpClient
      .post<LookbackReviewDto>(this.lookbackReviewEndpoint, lookbackReview, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  getLookbackTriggerCriteriaByCriteria(param: {
    [p: string]: string;
  }): Observable<HttpResponse<LookbackTriggerCriteriaDto[]>> {
    return this.httpClient
      .get<LookbackTriggerCriteriaDto[]>(this.lookbackTriggerCriteriaEndpoint, {
        observe: 'response',
        params: param,
      })
      .pipe(catchError(this.errorHandler));
  }
}
