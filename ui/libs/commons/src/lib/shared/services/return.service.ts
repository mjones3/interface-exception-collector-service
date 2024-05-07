import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class ReturnService {
  returnEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.returnEndpoint = config.env.serverApiURL + '/v1/returns';
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
