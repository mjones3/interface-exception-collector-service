import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReleaseTypeDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<ReleaseTypeDto>;
type EntityArrayResponseType = HttpResponse<ReleaseTypeDto[]>;

@Injectable({
  providedIn: 'root',
})
export class ReleaseTypeService {
  releaseTypeEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.releaseTypeEndpoint = config.env.serverApiURL + '/v1/release-types';
  }

  public getTypes(criteria?: { [key: string]: any }): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ReleaseTypeDto[]>(this.releaseTypeEndpoint, {
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
