import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DonorConsentDto } from '../models/donor-consent.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<DonorConsentDto>;
type EntityArrayResponseType = HttpResponse<DonorConsentDto[]>;

@Injectable({
  providedIn: 'root',
})
export class DonorConsentService {
  donorConsentEndpoint: string;
  createDonorConsentEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donorConsentEndpoint = config.env.serverApiURL + '/v1/consents';
    this.createDonorConsentEndpoint = this.donorConsentEndpoint;
  }

  public getDonorConsents(pageable): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonorConsentDto[]>(this.donorConsentEndpoint, { params: pageable, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorConsentByCriteria(params): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonorConsentDto[]>(this.donorConsentEndpoint, { params, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorConsentByDonationId(donationId: string): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonorConsentDto[]>(this.donorConsentEndpoint, { params: { donationId }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorConsentByDonationIdAndType(donationId: string, typeKey: string): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonorConsentDto[]>(this.donorConsentEndpoint, { params: { donationId, typeKey }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createDonorConsent(dto: DonorConsentDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<DonorConsentDto>(this.createDonorConsentEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
