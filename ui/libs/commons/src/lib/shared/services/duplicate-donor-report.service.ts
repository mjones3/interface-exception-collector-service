import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CountDto } from '../models';
import { DonorDto, DuplicateDonorDto, DuplicateDonorHistoriesDto } from '../models/donor.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<DonorDto[]>;
type EntityResponseType = HttpResponse<DonorDto>;

@Injectable({
  providedIn: 'root',
})
export class DuplicateDonorReportService {
  donorEndpoint: string;
  searchByCriteriaEndpoint: string;
  duplicateDonorReportsEndpoint: string;
  duplicateDonorStatusUpdateHistoryEndpoint: string;

  duplicateDonorsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donorEndpoint = config.env.serverApiURL + '/v1/donors';
    this.searchByCriteriaEndpoint = config.env.serverApiURL + '/v1/search/donors';
    this.duplicateDonorReportsEndpoint = config.env.serverApiURL + '/v1/duplicate-donors/report';
    this.duplicateDonorStatusUpdateHistoryEndpoint = config.env.serverApiURL + '/v1/duplicate-donor-histories';
    this.duplicateDonorsEndpoint = config.env.serverApiURL + '/v1/duplicate-donors';
  }

  public getDonor(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<DonorDto>(this.donorEndpoint + '/' + id, { observe: 'response' })
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

  public getDuplicateDonors(id: number): Observable<HttpResponse<DuplicateDonorDto>> {
    return this.httpClient
      .get<DuplicateDonorDto[]>(`${this.duplicateDonorsEndpoint}/${id}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDuplicateDonorReportsByCriteria(criteria: {}): Observable<HttpResponse<DuplicateDonorDto>> {
    return this.httpClient
      .get<DuplicateDonorDto[]>(this.duplicateDonorReportsEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDuplicateDonorStatusUpdateHistoryByCriteria(criteria: {}): Observable<
    HttpResponse<DuplicateDonorHistoriesDto[]>
  > {
    return this.httpClient
      .get<DuplicateDonorHistoriesDto[]>(this.duplicateDonorStatusUpdateHistoryEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDuplicateDonorStatusUpdateHistory(id: number): Observable<
    HttpResponse<DuplicateDonorHistoriesDto[]>
  > {
    return this.httpClient
      .get<DuplicateDonorHistoriesDto[]>(`${this.duplicateDonorStatusUpdateHistoryEndpoint}/${id}`, {
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

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
