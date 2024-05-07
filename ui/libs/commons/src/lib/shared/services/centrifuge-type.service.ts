import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CentrifugeTypeDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class CentrifugeTypeService {
  readonly centrifugeTypesUri = 'v1/centrifuge-types';
  readonly httpOptions: Object = {
    observe: 'response',
  };

  constructor(private http: HttpClient, public config: EnvironmentConfigService) {}

  getCentrifugeTypes(): Observable<HttpResponse<CentrifugeTypeDto[]>> {
    return this.http
      .get<CentrifugeTypeDto[]>(`${this.config.env.serverApiURL}/${this.centrifugeTypesUri}`, this.httpOptions)
      .pipe(catchError(this.errorHandler));
  }

  public findCentrifugeTypeById(centrifugeTypeId: number): Observable<HttpResponse<CentrifugeTypeDto>> {
    return this.http
      .get<CentrifugeTypeDto>(
        `${this.config.env.serverApiURL}/${this.centrifugeTypesUri}/${centrifugeTypeId}`,
        this.httpOptions
      )
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
