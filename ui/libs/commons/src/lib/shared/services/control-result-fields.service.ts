import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { FieldControlTypeDTO } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class ControlResultFieldsService {
  controlResultFieldsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.controlResultFieldsEndpoint = config.env.serverApiURL + '/v1/qc-control-result-fields';
  }

  public getControlResultFieldsByCriteria(params: any): Observable<HttpResponse<FieldControlTypeDTO[]>> {
    return this.httpClient
      .get<FieldControlTypeDTO[]>(this.controlResultFieldsEndpoint, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
