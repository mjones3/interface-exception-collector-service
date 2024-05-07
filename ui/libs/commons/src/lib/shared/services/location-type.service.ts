import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LocationDto, LocationTypeDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<LocationTypeDto>;

@Injectable({
  providedIn: 'root',
})
export class LocationTypeService {
  locationTypeEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.locationTypeEndpoint = config.env.serverApiURL + '/v1/location-types';
  }

  public getLocationType(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<LocationDto>(this.locationTypeEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
