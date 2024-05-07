import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DonorAddressAuditDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DonorAddressAuditService {
  private donorAddressAuditEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donorAddressAuditEndpoint = config.env.serverApiURL + '/v1/donor-addresses-audit';
  }

  fetchDonorAddressAudit(criteria: { [key: string]: any }): Observable<HttpResponse<DonorAddressAuditDto[]>> {
    return this.httpClient
      .get<DonorAddressAuditDto[]>(`${this.donorAddressAuditEndpoint}`, {
        params: criteria,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  private errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
