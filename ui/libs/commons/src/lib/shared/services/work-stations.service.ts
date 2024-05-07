import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EnvironmentConfigService } from './environment-config.service';

//type EntityResponseType = HttpResponse<WorkStationsDto>;

@Injectable({
  providedIn: 'root',
})
export class WorkStationsService {
  workStationsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.workStationsEndpoint = config.env.serverApiURL + '/v1/workstations';
  }

  public getWorkStationsByCriteria(criteria: {}): Observable<HttpResponse<any>> {
    return this.httpClient
      .get<any>(this.workStationsEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  private errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
