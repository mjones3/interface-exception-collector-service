import { HttpClient, HttpErrorResponse, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DeviceReportDto } from '../models/device-report.dto';
import { DeviceDto } from '../models/device.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<DeviceDto[]>;
type EntityReportArrayResponseType = HttpResponse<DeviceReportDto[]>;

@Injectable({
  providedIn: 'root',
})
export class DeviceService {
  deviceEndpoint: string;
  batchDevicesEndpoint: string;
  deviceReportEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.deviceEndpoint = config.env.serverApiURL + '/v1/devices';
    this.batchDevicesEndpoint = config.env.serverApiURL + '/v1/batch-devices/';
    this.deviceReportEndpoint = config.env.serverApiURL + '/v1/device-report';
  }

  //Get all devices
  public getAllDevices(
    typeId?: any,
    facilityId?: any,
    searchString?: string,
    filterBy?: any,
    page?: any
  ): Observable<EntityArrayResponseType> {
    const params = {
      'typeId.in': typeId && typeId.length ? [typeId] : null,
      facilityId: facilityId,
      [filterBy?.key]: searchString,
      includeInactive: 'true',
      page: page,
    };
    Object.keys(params).forEach(k => params[k] == null && delete params[k]);
    return this.httpClient
      .get<DeviceDto[]>(this.deviceEndpoint, {
        observe: 'response',
        params: params,
      })
      .pipe(catchError(this.errorHandler));
  }

  // add a device
  public addDevice(device: any) {
    return this.httpClient.post(this.deviceEndpoint, device).pipe(catchError(this.errorHandler));
  }

  // Add batch devices
  public addDevices(device: DeviceDto, barcodes: any) {
    const params = new HttpParams().set('barcodes', barcodes);
    return this.httpClient
      .post(this.batchDevicesEndpoint, device, { params: params })
      .pipe(catchError(this.errorHandler));
  }

  // get device by id
  public getDeviceById(id: any): Observable<HttpResponse<DeviceDto>> {
    return this.httpClient
      .get<DeviceDto>(this.deviceEndpoint + `/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // get device by id
  public getDeviceByIdAndFacility(id: any, facilityId: any): Observable<HttpResponse<DeviceDto>> {
    return this.httpClient
      .get<DeviceDto>(this.deviceEndpoint + `?typeId.in=${id}&facilityId=${facilityId}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDeviceByCriteria(criteria?: { [key: string]: any }): Observable<DeviceDto[]> {
    return this.httpClient
      .get<DeviceDto[]>(this.deviceEndpoint, { params: criteria })
      .pipe(catchError(this.errorHandler));
  }

  public getDeviceReportByCriteria(criteria, pageable, sortInfo?): Observable<EntityReportArrayResponseType> {
    return this.httpClient
      .get<DeviceReportDto[]>(this.deviceReportEndpoint, {
        params: { ...criteria, ...pageable, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  //get duplicate blood center id devices
  public getDeviceByBloodCenter(bloodCenterId: string): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DeviceDto[]>(this.deviceEndpoint + `?barcode=${bloodCenterId}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  public updateDevice(id, deviceDTO) {
    return this.httpClient.put(`${this.deviceEndpoint}/${id}`, deviceDTO).pipe(catchError(this.errorHandler));
  }
}
