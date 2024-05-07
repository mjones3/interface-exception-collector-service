import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LotDto, SterileConnectionDto, SterileConnectionFieldDto, SterileConnectionTypeDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';
import { FacilityService } from './facility.service';

type sterileArrayConnectionResponse = HttpResponse<SterileConnectionDto[]>;
type sterileConnectionResponse = HttpResponse<SterileConnectionDto>;

@Injectable({
  providedIn: 'root',
})
export class SterileConnectionService {
  readonly lotUrl: string;
  readonly sterileConnectionEndpoint: string;
  //private sterileConnectionProcessUrl = 'api/v1/sterile-connection-processes'; TODO
  readonly sterileConnectionTypeUrl: string;
  readonly sterileConnectionFieldUrl: string;

  constructor(
    private http: HttpClient,
    private facilityService: FacilityService,
    private envConfig: EnvironmentConfigService
  ) {
    this.lotUrl = envConfig.env.serverApiURL + '/v1/lots';
    this.sterileConnectionEndpoint = envConfig.env.serverApiURL + '/v1/sterile-connections';
    this.sterileConnectionTypeUrl = envConfig.env.serverApiURL + '/v1/sterile-connection-types';
    this.sterileConnectionFieldUrl = envConfig.env.serverApiURL + '/v1/sterile-connection-fields';
  }

  // create
  createSterileConnection(sterileConnection: SterileConnectionDto): Observable<sterileConnectionResponse> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    sterileConnection.id = null;
    return this.http
      .post<SterileConnectionDto>(this.sterileConnectionEndpoint, sterileConnection, {
        headers: headers,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // sterile-connection by inventory
  getSterileConnectionInventoryId(id: number): Observable<sterileArrayConnectionResponse> {
    const params = new HttpParams().set('inventoryId', String(id));
    return this.http
      .get<SterileConnectionDto[]>(this.sterileConnectionEndpoint, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Types by typeId
  getSterileConnectionType(typeId: number): Observable<HttpResponse<SterileConnectionTypeDto>> {
    const url = `${this.sterileConnectionTypeUrl}/${typeId}`;

    return this.http
      .get<SterileConnectionTypeDto>(url, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Types by Process
  getSterileConnectionTypesByProcess(
    process: string,
    processDescriptionKey?: string
  ): Observable<HttpResponse<SterileConnectionTypeDto[]>> {
    const url = processDescriptionKey
      ? `${this.sterileConnectionTypeUrl}?processKey=${process}&descriptionKey=${processDescriptionKey}`
      : `${this.sterileConnectionTypeUrl}?processKey=${process}`;

    return this.http
      .get<SterileConnectionTypeDto[]>(url, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Fields by Type
  getSterileConnectionFieldsByType(typeId: number): Observable<HttpResponse<SterileConnectionFieldDto[]>> {
    const url = `${this.sterileConnectionFieldUrl}?typeId=${typeId}`;
    return this.http
      .get<SterileConnectionFieldDto[]>(url, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Lots
  getLots(): Observable<HttpResponse<LotDto[]>> {
    const options = new HttpParams()
      .set('process', 'sterile-connection')
      .set('page', '0')
      .set('size', '100')
      .set('facilityId', `${this.facilityService.getFacilityId()}`);
    return this.http
      .get<LotDto[]>(this.lotUrl, { params: options, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  getLotsByTypeId(typeId: number): Observable<HttpResponse<LotDto[]>> {
    const url = `${this.lotUrl}?typeId=${typeId}&facilityId=${this.facilityService.getFacilityId()}`;
    return this.http
      .get<LotDto[]>(url, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  initializeSterileConnection(): SterileConnectionDto {
    // Return an initialized object
    return {
      id: 0,
      inventoryId: 0,
      typeId: 0,
      typeKey: '',
      employeeId: '',
      transferLot: '',
      waferLot: '',
      serialNumber: '',
      weldInspectionKey: '',
      reWeldKey: '',
      deleteDate: null,
      comments: '',
      processIndex: '',
      currentLotNumber: '',
      currentProcessIndex: '',
    };
  }

  initializeLot(): LotDto {
    // Return an initialized object
    return {
      id: 0,
      typeId: 0,
      facilityId: 0,
      employeeId: '',
      lotNumber: '',
    };
  }

  initializeSterileConnectionType(): SterileConnectionTypeDto {
    // Return an initialized object
    return {
      id: 0,
      descriptionKey: '',
      orderNumber: 0,
      active: false,
      updateLot: false,
      visualInspection: false,
    };
  }

  initializeSterileConnectionField(): SterileConnectionFieldDto {
    // Return an initialized object
    return {
      id: 0,
      descriptionKey: '',
      orderNumber: 0,
      active: false,
    };
  }

  updateSterileConnection(id: any, sterileConnection: any): Observable<HttpResponse<SterileConnectionDto>> {
    const url = `${this.sterileConnectionEndpoint}/${id}`;
    return this.http
      .put<SterileConnectionDto>(url, sterileConnection, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }
}
