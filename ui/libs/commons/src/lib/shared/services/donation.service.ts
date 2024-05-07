import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  DonationDiscardDto,
  DonationDto,
  DonationReviewDto,
  DonationReviewReportDto,
  EarlyDonationReportDto,
  EarlyDonationResolveDTO,
  TransactionResponseDto,
} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<DonationDto>;
type EntityArrayResponseType = HttpResponse<DonationDto[]>;

declare var dT_: any;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class DonationService {
  donationEndpoint: string;
  updateDonationEndpoint: string;
  deleteDonationEndpoint: string;
  findDonationEndpoint: string;
  findDonationByUnitNumberEndpoint: string;
  searchByCriteriaEndpoint: string;
  donationPackageEndpoint: string;
  donationDiscardsEndpoint: string;
  earlyDonationEndpoint: string;
  earlyDonationReportEndpoint: string;
  donationReviewReportsEndPoint: string;
  donationReviewEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
    this.donationEndpoint = config.env.serverApiURL + '/v1/donations';
    this.updateDonationEndpoint = this.donationEndpoint;
    this.deleteDonationEndpoint = this.donationEndpoint;
    this.findDonationEndpoint = this.donationEndpoint + '?unitNumber={unitNumber}';
    this.findDonationByUnitNumberEndpoint = this.donationEndpoint + '/unitNumber/{unitNumber}';
    this.searchByCriteriaEndpoint = config.env.serverApiURL + '/v1/search/donations';
    this.donationPackageEndpoint = config.env.serverApiURL + '/v1/prints/{fileType}/DONATION_PACKAGE';
    this.donationDiscardsEndpoint = config.env.serverApiURL + '/v1/donation-discards';
    this.earlyDonationEndpoint = config.env.serverApiURL + '/v1/early-donations';
    this.earlyDonationReportEndpoint = config.env.serverApiURL + '/v1/early-donation-report';
    this.donationReviewEndpoint = config.env.serverApiURL + '/v1/donation-reviews';
    this.donationReviewReportsEndPoint = `${this.donationReviewEndpoint}/donation-review-report`;
  }

  public getDonation(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<DonationDto>(this.donationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  /**
   * Get Donations based on criteria donorId
   */
  public getDonationsByDonorId(donorId: any, pageable?: any, sortInfo?): Observable<HttpResponse<DonationDto[]>> {
    const params = {
      donorId: donorId,
      ...pageable,
      ...sortInfo,
    };
    return this.httpClient
      .get<DonationDto[]>(this.donationEndpoint, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationByCriteria(params): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonationDto[]>(this.donationEndpoint, {
        params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createDonation(dto: DonationDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<DonationDto>(this.donationEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updatedDonationBagTypes(dto: any, donationId: string): Observable<HttpResponse<TransactionResponseDto>> {
    const url = `${this.donationEndpoint}/${donationId}/bag-type-edits`;
    return this.httpClient
      .post<DonationDto>(url, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public convertCodabar(dto: any, donationId: number): Observable<HttpResponse<TransactionResponseDto>> {
    const url = `${this.donationEndpoint}/${donationId}/convert-codabar`;
    return this.httpClient
      .post<DonationDto>(url, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateDonation(id: number, dto: DonationDto): Observable<EntityResponseType> {
    return this.httpClient
      .put<DonationDto>(this.updateDonationEndpoint + '/' + id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteDonation(id: number, dto: DonationDto): Observable<EntityResponseType> {
    return this.httpClient
      .delete<DonationDto>(this.deleteDonationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public searchDonation(id: number, dto: DonationDto): Observable<EntityArrayResponseType> {
    return this.httpClient
      .put<DonationDto[]>(this.replaceValueInUrl(this.donationEndpoint, id), dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public searchSingleDonationByUnitNumber(unitNumber: any): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonationDto[]>(this.replaceValueInUrl(this.findDonationEndpoint, unitNumber), { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationByFilter(criteria, pageable, sortInfo?): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonationDto[]>(this.searchByCriteriaEndpoint, {
        params: { ...criteria, ...pageable, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationDiscardsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<DonationDiscardDto[]>> {
    return this.httpClient
      .get<DonationDiscardDto[]>(this.donationDiscardsEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationByUnitNumber(unitNumber: string): Observable<EntityResponseType> {
    return this.httpClient
      .get<DonationDto>(this.replaceValueInUrl(this.findDonationByUnitNumberEndpoint, unitNumber), {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getLatestDonationFromDonorBeforeDate(
    donorId: number,
    date: Date,
    sortInfo?
  ): Observable<EntityArrayResponseType> {
    const params = {
      'donationDate.lessThan': date,
      donorId: donorId,
      ...sortInfo,
    };
    return this.httpClient
      .get<DonationDto[]>(this.donationEndpoint, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  //#region EARLY DONATION

  public getEarlyDonationReportByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<EarlyDonationReportDto[]>> {
    return this.httpClient
      .get<EarlyDonationReportDto[]>(this.earlyDonationReportEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public resolveEarlyDonation(id: number, dto: EarlyDonationResolveDTO): Observable<TransactionResponseDto> {
    return this.httpClient
      .put<TransactionResponseDto>(`${this.earlyDonationEndpoint}/${id}/resolve`, dto)
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region DONATION REVIEW

  public getDonationReviewReportByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<DonationReviewReportDto[]>> {
    return this.httpClient
      .get<DonationReviewReportDto[]>(this.donationReviewReportsEndPoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationReviewById(id: number): Observable<DonationReviewDto> {
    return this.httpClient
      .get<DonationReviewDto>(this.donationReviewEndpoint + '/' + id)
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  private replaceValueInUrl(url: string, value: any): string {
    return url.slice().replace('{unitNumber}', String(value));
  }
}
