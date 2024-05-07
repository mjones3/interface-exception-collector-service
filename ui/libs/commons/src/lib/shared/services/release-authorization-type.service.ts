import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReleaseAuthorizationTypeDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<ReleaseAuthorizationTypeDto>;
type EntityArrayResponseType = HttpResponse<ReleaseAuthorizationTypeDto[]>;

@Injectable({
  providedIn: 'root',
})
export class ReleaseAuthorizationTypeService {
  releaseAuthorizationTypeEndpoint: string;
  releaseAuthorizationTypeByItemsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.releaseAuthorizationTypeEndpoint = config.env.serverApiURL + '/v1/authorization-types';
    this.releaseAuthorizationTypeByItemsEndpoint = `${this.releaseAuthorizationTypeEndpoint}-items`;
  }

  public getAllAuthorizationTypes(criteria?: { [key: string]: string }): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ReleaseAuthorizationTypeDto[]>(this.releaseAuthorizationTypeEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAuthorizationTypesByItemsIds(releaseItemsIds: string): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ReleaseAuthorizationTypeDto[]>(this.releaseAuthorizationTypeByItemsEndpoint + '/' + releaseItemsIds, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
