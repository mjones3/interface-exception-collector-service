import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CountDto, DonorAddressDto, DonorContactPointDto, DonorIdDto, DonorNameDto } from '../models';
import { DonorReentryDto } from '../models/donor-reentry.dto';
import { DonorDto } from '../models/donor.dto';
import { DonorNotificationHistoryDto } from '../models/notification.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<DonorDto[]>;
type EntityResponseType = HttpResponse<DonorDto>;
type ReentryResponseType = HttpResponse<DonorReentryDto[]>;
type EntityArrayResponseNotificationHistory = HttpResponse<DonorNotificationHistoryDto[]>;

declare var dT_: any;

@Injectable({
  providedIn: 'root',
})
export class DonorService {
  donorEndpoint: string;
  searchByCriteriaEndpoint: string;
  searchByDonorsAndDonationsByFilter: string;
  donorReentryEndpoint: string;
  donorNotificationReportEndpoint: string;
  donorNotificationEndpoint: string;
  donorNameEndpoint: string;
  donorContactPointEndpoint: string;
  donorAddressEndpoint: string;
  donorIdEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
    this.donorEndpoint = config.env.serverApiURL + '/v1/donors';
    this.searchByCriteriaEndpoint = config.env.serverApiURL + '/v1/search/donors';
    this.donorReentryEndpoint = config.env.serverApiURL + '/v1/donor-reentries';
    this.donorNotificationReportEndpoint = config.env.serverApiURL + '/v1/donor-notifications/report';
    this.searchByDonorsAndDonationsByFilter = config.env.serverApiURL + '/v1/donor-notifications/on-demand/report';
    this.donorNotificationEndpoint = config.env.serverApiURL + '/v1/donor-notifications';
    this.donorNameEndpoint = config.env.serverApiURL + '/v1/donor-names';
    this.donorContactPointEndpoint = config.env.serverApiURL + '/v1/donor-contact-points';
    this.donorAddressEndpoint = config.env.serverApiURL + '/v1/donor-addresses';
    this.donorIdEndpoint = config.env.serverApiURL + '/v1/donor-ids';
  }

  public getDonor(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<DonorDto>(this.donorEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorsByCriteria(criteria, pageable?, sortInfo?): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonorDto[]>(this.donorEndpoint, {
        params: { ...criteria, ...pageable, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorsByFilter(criteria, pageable, sortInfo?): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonorDto[]>(this.searchByCriteriaEndpoint, {
        params: { ...criteria, ...pageable, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorsAndDonationsByFilter(criteria, pageable, sortInfo?): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonorDto[]>(this.searchByDonorsAndDonationsByFilter, {
        params: { ...criteria, ...pageable, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorNotificationByFilter(
    criteria,
    pageable,
    sortInfo?
  ): Observable<EntityArrayResponseNotificationHistory> {
    return this.httpClient
      .get<DonorNotificationHistoryDto[]>(this.donorNotificationReportEndpoint, {
        params: { ...criteria, ...pageable, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public updateDonor(id: number, dto: DonorDto): Observable<HttpResponse<DonorDto>> {
    return this.httpClient
      .put<DonorDto>(this.donorEndpoint + '/' + id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  /**
   * Get year to date donation count for the donor
   * @param id id of the Donor
   * @return CountDto
   */
  public getYearToDateDonationCount(id: number): Observable<HttpResponse<CountDto>> {
    return this.httpClient
      .get<CountDto>(this.donorEndpoint + '/' + id + '/total-donations', { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  /**
   * Get HLA traly flaf from the donor
   * @param donor
   * @return true/false
   */
  public getHltTralyFlagFromDonor(donor) {
    return donor.properties['TAG_HLA_TRALI_RISK'] === 'Y' || donor.properties['TAG_HLA_TRALI_RESULT'] === 'POSITIVE';
  }

  public getDonorReentries(id: number): Observable<ReentryResponseType> {
    return this.httpClient
      .get<DonorReentryDto>(this.donorReentryEndpoint + '?donorId=' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#region Donor Names

  public getDonorNameById(id: number): Observable<HttpResponse<DonorNameDto>> {
    return this.httpClient
      .get<DonorNameDto>(this.donorNameEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorNamesByDonorId(donorId: number): Observable<HttpResponse<DonorNameDto[]>> {
    return this.httpClient
      .get<DonorNameDto[]>(this.donorNameEndpoint + '?donorId=' + donorId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorNamesByCriteria(criteria: object): Observable<HttpResponse<DonorNameDto[]>> {
    return this.httpClient
      .get<DonorNameDto[]>(this.donorNameEndpoint, {
        observe: 'response',
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public createDonoName(dto: DonorNameDto): Observable<HttpResponse<DonorNameDto>> {
    return this.httpClient
      .post<DonorNameDto>(this.donorNameEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateDonorName(dto: DonorNameDto): Observable<HttpResponse<DonorNameDto>> {
    return this.httpClient
      .put<DonorNameDto>(this.donorNameEndpoint + '/' + dto.id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteDonorName(id: number): Observable<HttpResponse<DonorNameDto>> {
    return this.httpClient
      .delete<DonorNameDto>(this.donorNameEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region Donor Contact Point

  public getDonorContactPointById(id: number): Observable<HttpResponse<DonorContactPointDto>> {
    return this.httpClient
      .get<DonorContactPointDto>(this.donorContactPointEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorContactPointsByDonorId(donorId: number): Observable<HttpResponse<DonorContactPointDto[]>> {
    return this.httpClient
      .get<DonorContactPointDto[]>(this.donorContactPointEndpoint + '?donorId=' + donorId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorContactPointsByCriteria(criteria: object): Observable<HttpResponse<DonorContactPointDto[]>> {
    return this.httpClient
      .get<DonorContactPointDto[]>(this.donorContactPointEndpoint, {
        observe: 'response',
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public createDonorContactPoint(dto: DonorContactPointDto): Observable<HttpResponse<DonorContactPointDto>> {
    return this.httpClient
      .post<DonorContactPointDto>(this.donorContactPointEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateDonorContactPoint(dto: DonorContactPointDto): Observable<HttpResponse<DonorContactPointDto>> {
    return this.httpClient
      .put<DonorContactPointDto>(this.donorContactPointEndpoint + '/' + dto.id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteDonorContactPoint(id: number): Observable<HttpResponse<DonorContactPointDto>> {
    return this.httpClient
      .delete<DonorContactPointDto>(this.donorContactPointEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region Donor Address

  public getDonorAddressById(id: number): Observable<HttpResponse<DonorAddressDto>> {
    return this.httpClient
      .get<DonorAddressDto>(this.donorAddressEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorAddressesByDonorId(donorId: number): Observable<HttpResponse<DonorAddressDto[]>> {
    return this.httpClient
      .get<DonorAddressDto[]>(this.donorAddressEndpoint + '?donorId=' + donorId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorAddressesByCriteria(criteria: object): Observable<HttpResponse<DonorAddressDto[]>> {
    return this.httpClient
      .get<DonorAddressDto[]>(this.donorAddressEndpoint, {
        observe: 'response',
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public createDonorAddress(dto: DonorAddressDto): Observable<HttpResponse<DonorAddressDto>> {
    return this.httpClient
      .post<DonorAddressDto>(this.donorAddressEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateDonorAddress(dto: DonorAddressDto): Observable<HttpResponse<DonorAddressDto>> {
    return this.httpClient
      .put<DonorAddressDto>(this.donorAddressEndpoint + '/' + dto.id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteDonorAddress(id: number): Observable<HttpResponse<DonorAddressDto>> {
    return this.httpClient
      .delete<DonorAddressDto>(this.donorAddressEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region Donor Id

  public getDonorIdById(id: number): Observable<HttpResponse<DonorIdDto>> {
    return this.httpClient
      .get<DonorIdDto>(this.donorIdEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorIdsByDonorId(donorId: number): Observable<HttpResponse<DonorIdDto[]>> {
    return this.httpClient
      .get<DonorIdDto[]>(this.donorIdEndpoint + '?donorId=' + donorId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
