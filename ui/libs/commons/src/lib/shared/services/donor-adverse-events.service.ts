import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DonorAdverseEventDto } from '../models/donor-adverse-event.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DonorAdverseEventsService {
  donorAdverseEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donorAdverseEndpoint = config.env.serverApiURL + '/v1/donor-adverse-events';
  }

  public getDonorAdverseEventsByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<DonorAdverseEventDto[]>> {
    return this.httpClient
      .get<DonorAdverseEventDto[]>(this.donorAdverseEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
