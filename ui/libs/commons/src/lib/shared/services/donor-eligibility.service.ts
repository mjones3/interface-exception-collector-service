import {HttpClient, HttpErrorResponse, HttpResponse} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DhqDeferredDonorReportDTO } from '../models';
import { DhqDeferredDonorReviewDto } from '../models/dhq-deferred-donor-review.dto';
import { DhqDeferredDonorDto } from '../models/dhq-deferred-donor.dto';
import { DonorEligibilityDto } from '../models/donor-eligibility.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<DhqDeferredDonorReviewDto[]>;
type DhqDeferredDonorResponseType = HttpResponse<DhqDeferredDonorDto>;

@Injectable({
  providedIn: 'root',
})
export class DonorEligibilityService {
  donorEligibilityEndPoint: string;
  dhqDeferredDonorReportEndpoint: string;
  dhqDeferredDonorReport: string;
  dhqDeferredDonorReview: string;
  dhqDeferredDonorReviewFromCurrentUser: string;
  dhqDeferredDonor: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.dhqDeferredDonorReportEndpoint = config.env.serverApiURL + '/v1/dhq-deferred-donors/report';
    this.donorEligibilityEndPoint = config.env.serverApiURL + '/v1/donor-eligibilities';
    this.dhqDeferredDonorReport = config.env.serverApiURL + '/v1/dhq-deferred-donor-reports';
    this.dhqDeferredDonorReview = config.env.serverApiURL + '/v1/dhq-deferred-donor-reviews';
    this.dhqDeferredDonorReviewFromCurrentUser =
      config.env.serverApiURL + '/v1/dhq-deferred-donor-reviews/current-users';
    this.dhqDeferredDonor = config.env.serverApiURL + '/v1/dhq-deferred-donors';
  }

  /**
   * Get Donor Eligibility using Donor ID and criteria
   */
  public getDonorEligibilitiesByCriteria(
    donorId: string,
    criteria?: { [key: string]: string | string[] },
  ): Observable<HttpResponse<DonorEligibilityDto[]>> {
    return this.httpClient
      .get<DonorEligibilityDto[]>(this.donorEligibilityEndPoint, {
        observe: 'response',
        params: {
          donorId,
          ...criteria,
        },
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDhqDeferralDonorReportByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<DhqDeferredDonorReportDTO[]>> {
    return this.httpClient
      .get<DhqDeferredDonorReportDTO[]>(this.dhqDeferredDonorReportEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDHQDeferredDonorReviewsByDeferredDonorId(
    pageable,
    params: { dhqDeferredDonorId?: number },
    sortInfo?
  ): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DhqDeferredDonorReviewDto[]>(this.dhqDeferredDonorReview, {
        params: { ...pageable, ...params, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDHQDeferredDonorById(dhqDeferredDonorId): Observable<DhqDeferredDonorResponseType> {
    return this.httpClient
      .get<DhqDeferredDonorDto>(`${this.dhqDeferredDonor}/${dhqDeferredDonorId}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createReviewDecision(dto: DhqDeferredDonorReviewDto): Observable<HttpResponse<DhqDeferredDonorReviewDto>> {
    return this.httpClient
      .post<DhqDeferredDonorReviewDto>(this.dhqDeferredDonorReview, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDHQReviewsOfCurrentUser(
    criteria?: {
      [key: string]: any;
    },
    sortInfo = { sort: 'createDate,desc' }
  ): Observable<HttpResponse<DhqDeferredDonorReviewDto[]>> {
    return this.httpClient
      .get<DhqDeferredDonorReviewDto[]>(this.dhqDeferredDonorReviewFromCurrentUser, {
        params: { ...criteria, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
