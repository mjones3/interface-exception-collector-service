import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LocationTypeDto } from '../models/location-types.dto';
import { LocationAddressDto, LocationDto, LocationGroupDto } from '../models/location.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<LocationDto>;
type EntityArrayResponseType = HttpResponse<LocationDto[]>;
type EntityArrayResponseLocationType = HttpResponse<LocationTypeDto[]>;

@Injectable({
  providedIn: 'root',
})
export class LocationService {
  locationEndpoint: string;
  locationTypeEndPoint: string;
  locationGroupEndpoint: string;
  locationAddressEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.locationEndpoint = config.env.serverApiURL + '/v1/locations';
    this.locationTypeEndPoint = config.env.serverApiURL + '/v1/location-types';
    this.locationGroupEndpoint = config.env.serverApiURL + '/v1/location-groups';
    this.locationAddressEndpoint = config.env.serverApiURL + '/v1/location-addresses';
  }

  public getLocation(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<LocationDto>(this.locationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getAllLocations(propertyKey?: string): Observable<EntityArrayResponseType> {
    const params = {
      page: '0',
      size: '1000',
      sort: 'name,ASC',
    };

    if (propertyKey) {
      params['propertyKey'] = propertyKey;
    }

    return this.httpClient
      .get<LocationDto>(this.locationEndpoint, {
        params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAllLocationsByCriteria(criteria: {}): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<LocationDto[]>(this.locationEndpoint, {
        params: { ...criteria, sort: 'name,ASC' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAllLocationAddressByCriteria(criteria: {}): Observable<HttpResponse<LocationAddressDto[]>> {
    return this.httpClient
      .get<LocationAddressDto[]>(this.locationAddressEndpoint, {
        params: { ...criteria, sort: 'city,ASC' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getLocationTypes(): Observable<EntityArrayResponseLocationType> {
    return this.httpClient
      .get<LocationTypeDto[]>(this.locationTypeEndPoint, {
        params: { active: 'true' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getLocationTypesByCriteria(criteria: {}): Observable<EntityArrayResponseLocationType> {
    return this.httpClient
      .get<LocationTypeDto[]>(this.locationTypeEndPoint, {
        params: { ...criteria, active: 'true' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  //LOCATION GROUP
  public getLocationGroupsByCriteria(criteria?: { [key: string]: any }): Observable<LocationGroupDto[]> {
    return this.httpClient
      .get<LocationGroupDto[]>(this.locationGroupEndpoint, {
        params: { ...criteria, sort: 'name,ASC' },
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
