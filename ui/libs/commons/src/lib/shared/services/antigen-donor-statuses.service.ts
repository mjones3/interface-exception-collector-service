import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AntigenDonorStatusSummaryDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class AntigenDonorStatusesService {
  antigenDonorStatusesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.antigenDonorStatusesEndpoint = config.env.serverApiURL + '/v1/antigen-donor-status';
  }

  public getAntigenDonorSummary(donorId: number | string): Observable<HttpResponse<AntigenDonorStatusSummaryDto>> {
    return this.httpClient
      .get<AntigenDonorStatusSummaryDto>(`${this.antigenDonorStatusesEndpoint}/${donorId}/summary`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
