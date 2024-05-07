import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DonorAddressCorrectionDto, DonorAddressCorrectionReportDto, DonorAddressStatusUpdateDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DonorAddressCorrectionReportService {
  private donorAddressCorrectionReportEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donorAddressCorrectionReportEndpoint = config.env.serverApiURL + '/v1/donor-address-corrections';
  }

  fetchDonorAddressCorrectionReport(criteria: {
    [key: string]: any;
  }): Observable<HttpResponse<DonorAddressCorrectionReportDto[]>> {
    return this.httpClient
      .get<DonorAddressCorrectionReportDto[]>(`${this.donorAddressCorrectionReportEndpoint}/report`, {
        params: criteria,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  getDonorAddressCorrectionById(id: string): Observable<HttpResponse<DonorAddressCorrectionDto>> {
    return this.httpClient
      .get<DonorAddressCorrectionDto>(`${this.donorAddressCorrectionReportEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  updateDonorAddressCorrectionStatus(
    id: string,
    addressStatusUpdateDto: DonorAddressStatusUpdateDto
  ): Observable<{ status: string }> {
    return this.httpClient
      .post<{ status: string }>(
        `${this.donorAddressCorrectionReportEndpoint}/${id}/update-status`,
        addressStatusUpdateDto
      )
      .pipe(catchError(this.errorHandler));
  }

  updateDonorAddress(
    id: string | number,
    donorAddressCorrectionDto: DonorAddressCorrectionDto
  ): Observable<HttpResponse<DonorAddressCorrectionDto>> {
    return this.httpClient
      .post<DonorAddressCorrectionDto>(
        `${this.donorAddressCorrectionReportEndpoint}/${id}/update-address`,
        donorAddressCorrectionDto
      )
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  private errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
