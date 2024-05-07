import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DeviceTypeDto } from '../models/device-type.dto';
import { EnvironmentConfigService } from './environment-config.service';

type DeviceTypeResponse = HttpResponse<DeviceTypeDto>;
type EntityArrayResponseType = HttpResponse<DeviceTypeDto[]>;

@Injectable({
  providedIn: 'root',
})
export class DeviceTypesService {
  dataFound: boolean;
  deviceTypeEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.deviceTypeEndpoint = config.env.serverApiURL + '/v1/device-types';
  }

  // Get Device Type
  public getDeviceType(id: number): Observable<DeviceTypeResponse> {
    return this.httpClient
      .get<DeviceTypeResponse>(`${this.deviceTypeEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //Get All Device Types
  public getAllDeviceTypes(): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DeviceTypeDto[]>(this.deviceTypeEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getAllDeviceTypesByCriteria(criteria?: { [key: string]: any }): Observable<DeviceTypeDto[]> {
    return this.httpClient
      .get<DeviceTypeDto[]>(this.deviceTypeEndpoint, { params: { ...criteria } })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
