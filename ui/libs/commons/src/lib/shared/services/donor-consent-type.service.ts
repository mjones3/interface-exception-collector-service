import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DonorConsentsDto, DonorConsentTypeDto } from '../models/donor-consent-type.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DonorConsentTypeService {
  donorConsentTypeEndpoint: string;
  donorConsentsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donorConsentTypeEndpoint = config.env.serverApiURL + '/v1/consent-types';
    this.donorConsentsEndpoint = config.env.serverApiURL + '/v1/consents';
  }

  public getDonorConsentTypes(): Observable<HttpResponse<DonorConsentTypeDto[]>> {
    return this.httpClient
      .get<DonorConsentTypeDto[]>(this.donorConsentTypeEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorConsentsByDonationId(id: number): Observable<HttpResponse<DonorConsentsDto[]>> {
    return this.httpClient
      .get<DonorConsentTypeDto[]>(this.donorConsentsEndpoint + `?donationId=${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
