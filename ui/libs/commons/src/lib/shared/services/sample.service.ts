import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class SampleService {

  sampleEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.sampleEndpoint = config.env.serverApiURL + '/v1/pqc/sample-transactions';
  }

  submitSampleVerification(request:any): Observable<any> {
    // Todo on Integration: Create Logic for Submit Sample Verification Page
    return this.httpClient
      .post<any>(this.sampleEndpoint, request, { observe: 'response' })
      .pipe(catchError(this.errorHandler));


  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
