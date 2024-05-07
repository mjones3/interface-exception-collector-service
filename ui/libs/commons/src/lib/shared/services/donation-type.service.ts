import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {DonationTypeDto, LocationDto} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<DonationTypeDto[]>;
type EntityResponseType = HttpResponse<DonationTypeDto>;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class DonationTypeService {
  donationTypeEndpoint: string;
  updateDonationTypeEndpoint: string;
  deleteDonationTypeEndpoint: string;
  donationTypeByMotivationsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donationTypeEndpoint = config.env.serverApiURL + '/v1/donation-types';
  }

  public getDonationTypes(pageSize: number): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonationTypeDto[]>(`${this.donationTypeEndpoint}?size=${pageSize}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationTypesByCriteria(criteria: {}): Observable<EntityArrayResponseType> {

    return this.httpClient
      .get<DonationTypeDto>(this.donationTypeEndpoint, {
        params: { ...criteria},
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));

  }


  public getDonationType(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<DonationTypeDto>(this.donationTypeEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationTypeByMotivation(motivationId: string): Observable<EntityResponseType> {
    this.getDonationTypeByMotivationsEndpoint(motivationId);
    return this.httpClient
      .get<DonationTypeDto>(this.donationTypeByMotivationsEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createDonationType(dto: DonationTypeDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<DonationTypeDto>(this.donationTypeEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateDonationType(id: number, dto: DonationTypeDto): Observable<EntityResponseType> {
    return this.httpClient
      .put<DonationTypeDto>(this.donationTypeEndpoint + '/' + id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteDonationType(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .delete<DonationTypeDto>(this.donationTypeEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  public getDonationTypeByMotivationsEndpoint(motivationId): string {
    this.donationTypeByMotivationsEndpoint = `${this.config.env.serverApiURL}/v1/motivations/${motivationId}/donation-types`;
    return this.donationTypeByMotivationsEndpoint;
  }
}
