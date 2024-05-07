import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, delay } from 'rxjs/operators';
import {
  EventManagementCriteriaDto,
  EventManagementDonorDetailsDto,
  EventManagementDto,
  EventManagementProductActionDto,
  EventManagementProductDetailsDto,
  EventManagementReviewDto,
  EventManagementReviewTypesDto,
  ProcessingStatusDto,
} from '../models/event-management.dto';
import { EnvironmentConfigService } from './environment-config.service';

//Common rest
@Injectable({
  providedIn: 'root',
})
export class EventManagementService {
  eventManagementEndpoint: string;
  eventManagementCriteriaEndpoint: string;
  eventManagementReviewEndpoint: string;
  eventManagementReviewTypesEndpoint: string;
  eventManagementDonorDetailsEndpoint: string;
  eventManagementProductDetailsEndpoint: string;
  eventManagementProductActionEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.eventManagementEndpoint = config.env.serverApiURL + '/v1/event-managements';
    this.eventManagementCriteriaEndpoint = config.env.serverApiURL + '/v1/event-management-criterias';
    this.eventManagementReviewEndpoint = config.env.serverApiURL + '/v1/event-management-reviews';
    this.eventManagementProductActionEndpoint = config.env.serverApiURL + '/v1/event-management-product-actions';
    this.eventManagementReviewTypesEndpoint = config.env.serverApiURL + '/v1/event-management-review-types';
    this.eventManagementDonorDetailsEndpoint = config.env.serverApiURL + '/v1/event-management-donor-details';
    this.eventManagementProductDetailsEndpoint = config.env.serverApiURL + '/v1/event-management-product-details';
  }

  //EVENT MANAGEMENT

  public getEventManagementByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<EventManagementDto[]>> {
    return this.httpClient
      .get<EventManagementDto[]>(this.eventManagementEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public create(dto: EventManagementDto): Observable<EventManagementDto> {
    return this.httpClient
      .post<EventManagementDto>(this.eventManagementEndpoint, dto)
      .pipe(catchError(this.errorHandler));
  }

  public getEventManagementStatus(id: number): Observable<ProcessingStatusDto> {
    return this.httpClient
      .get<ProcessingStatusDto>(`${this.eventManagementEndpoint}/${id}/status`)
      .pipe(catchError(this.errorHandler), delay(1));
  }

  public update(id: number, dto: EventManagementDto): Observable<EventManagementDto> {
    return this.httpClient
      .put<EventManagementDto>(`${this.eventManagementEndpoint}/${id}`, dto)
      .pipe(catchError(this.errorHandler));
  }

  //EVENT MANAGEMENT CRITERIA

  public getEventManagementCriteriaByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<EventManagementCriteriaDto[]>> {
    return this.httpClient
      .get<EventManagementCriteriaDto[]>(this.eventManagementCriteriaEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createCriteria(dto: EventManagementCriteriaDto): Observable<EventManagementCriteriaDto> {
    return this.httpClient
      .post<EventManagementCriteriaDto>(this.eventManagementCriteriaEndpoint, dto)
      .pipe(catchError(this.errorHandler), delay(1));
  }

  //EVENT MANAGEMENT REVIEW

  public getEventManagementReviewsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<EventManagementReviewDto[]>> {
    return this.httpClient
      .get<EventManagementReviewDto[]>(this.eventManagementReviewEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getEventReviewTypesByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<EventManagementReviewTypesDto[]>> {
    return this.httpClient
      .get<EventManagementReviewTypesDto[]>(this.eventManagementReviewTypesEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createReview(dto: EventManagementReviewDto): Observable<EventManagementReviewDto> {
    return this.httpClient
      .post<EventManagementReviewDto>(this.eventManagementReviewEndpoint, dto)
      .pipe(catchError(this.errorHandler));
  }

  //EVENT MANAGEMENT DONORS

  public getDonorDetailsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<EventManagementDonorDetailsDto[]>> {
    return this.httpClient
      .get<EventManagementDonorDetailsDto[]>(this.eventManagementDonorDetailsEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  //EVENT MANAGEMENT PRODUCTS

  public getProductDetailsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<EventManagementProductDetailsDto[]>> {
    return this.httpClient
      .get<EventManagementProductDetailsDto[]>(this.eventManagementProductDetailsEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createProductAction(dto: EventManagementProductActionDto): Observable<EventManagementProductActionDto> {
    return this.httpClient
      .post<EventManagementProductActionDto>(this.eventManagementProductActionEndpoint, dto)
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
