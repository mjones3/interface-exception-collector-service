import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BagTypeDto, DonationTypeDto, DonorIntentionDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<DonorIntentionDto>;
type EntityArrayResponseType = HttpResponse<DonorIntentionDto[]>;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class DonorIntentionService {
  donorIntentionEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donorIntentionEndpoint = config.env.serverApiURL + '/v1/motivations';
  }

  public getDonorIntentions(pageSize: number): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonorIntentionDto[]>(`${this.donorIntentionEndpoint}?size=${pageSize}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorIntention(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<DonorIntentionDto>(this.donorIntentionEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createDonorIntention(dto: DonorIntentionDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<DonorIntentionDto>(this.donorIntentionEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateDonorIntention(id: number, dto: DonorIntentionDto): Observable<EntityResponseType> {
    return this.httpClient
      .put<DonorIntentionDto>(this.donorIntentionEndpoint + '/' + id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteDonorIntention(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .delete<DonorIntentionDto>(this.donorIntentionEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationTypesByDonorIntention(id: number): Observable<HttpResponse<DonationTypeDto[]>> {
    return this.httpClient
      .get<DonationTypeDto[]>(this.donorIntentionEndpoint + '/' + id + '/donation-types', { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getBagTypesByDonorIntentionAndDonationType(
    id: number,
    donationTypeId: number
  ): Observable<HttpResponse<BagTypeDto[]>> {
    return this.httpClient
      .get<BagTypeDto[]>(this.donorIntentionEndpoint + '/' + id + '/donation-types/' + donationTypeId + '/bag-types', {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
