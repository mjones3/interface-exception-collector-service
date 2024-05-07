import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DonationQuarantineDto } from '../models/donation-quarantine.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<DonationQuarantineDto>;
type EntityArrayResponseType = HttpResponse<DonationQuarantineDto[]>;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class DonationQuarantineService {
  donationQuarantineEndpoint: string;
  donationQuarantineReportEndpoint: string;
  updateDonationEndpoint: string;
  deleteDonationEndpoint: string;
  findDonationEndpoint: string;
  findDonationByUnitNumberEndpoint: string;
  searchByCriteriaEndpoint: string;
  donationPackageEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donationQuarantineEndpoint = config.env.serverApiURL + '/v1/donation-quarantines';
    this.donationQuarantineReportEndpoint =
      this.donationQuarantineEndpoint + '/report?quarantineStatusKey=active.label';
  }

  public getDonationQuarantineHistory(donationId: number): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonationQuarantineDto[]>(this.donationQuarantineEndpoint + '?donationId=' + donationId, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createQuarantineHistory(
    donationId: number,
    facilityId: number,
    employeeId: string,
    comments: string,
    reasonKey: string
  ): Observable<EntityResponseType> {
    let dto: DonationQuarantineDto;
    dto = {
      donationId: donationId,
      facilityId: facilityId,
      employeeId: employeeId,
      comments: comments,
      reasonKey: reasonKey,
      quarantineStatusKey: 'active.label',
    };

    return this.httpClient
      .post<DonationQuarantineDto>(this.donationQuarantineEndpoint + '?donationId=' + donationId, dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public deactivateQuarantine(
    id: number,
    employeeId: string | number,
    comments: string,
    donationId: number,
    facilityId: number
  ): Observable<EntityResponseType> {
    let dto: DonationQuarantineDto;
    dto = {
      donationId: donationId,
      facilityId: facilityId,
      deactivationEmployeeId: employeeId as string,
      deactivationComments: comments,
      quarantineStatusKey: 'inactive.label',
    };

    return this.httpClient
      .put<DonationQuarantineDto>(this.donationQuarantineEndpoint + '/' + id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationQuarantineByCriteria(criteria?: { [key: string]: any }): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonationQuarantineDto[]>(this.donationQuarantineEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationQuarantineById(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<DonationQuarantineDto>(`${this.donationQuarantineEndpoint}/${id}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationQuarantineReport(unitNumber: string): Observable<EntityArrayResponseType> {
    const filter = unitNumber ? '&unitNumber=' + unitNumber : '';

    return this.httpClient
      .get<DonationQuarantineDto[]>(this.donationQuarantineReportEndpoint + filter, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
