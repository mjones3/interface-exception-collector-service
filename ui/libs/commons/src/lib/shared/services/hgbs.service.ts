import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HgbsKitEntryDTO } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class HgbsService {
  hgbsKitEntryEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.hgbsKitEntryEndpoint = config.env.serverApiURL + '/v1/hgbs-kit-entries';
  }

  public getHgbsKitEntryByCriteria(criteria: {}): Observable<HttpResponse<HgbsKitEntryDTO[]>> {
    return this.httpClient
      .get<any>(this.hgbsKitEntryEndpoint, {
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
