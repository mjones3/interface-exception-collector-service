import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LookbackReviewDto } from '../models/lookback-review.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class LookbackReviewService {
  lookbackEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.lookbackEndpoint = config.env.serverApiURL + '/v1/lookback-review';
  }

  public getLookbackReviewByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<LookbackReviewDto[]>> {
    return this.httpClient
      .get<any[]>(`${this.lookbackEndpoint}`, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
