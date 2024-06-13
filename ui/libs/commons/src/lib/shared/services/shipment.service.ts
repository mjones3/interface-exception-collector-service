import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import * as moment from 'moment';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  CurrentTimeDto,
  Description,
  LocationInventoryHistoryDto,
  ReturnsDto,
  RuleResponseDto,
  ShipmentDto,
  ShipmentInfoDto,
  TransitTimeRequestDto,
  TransitTimeResponseDto,
  VerifyProduct,
} from '../models';
import { ExternalTransferDto } from '../models/external-transfer.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class ShipmentService {
  shipmentEndpoint: string;
  transitTimeEndpoint: string;
  returnsEndpoint: string;
  externalTransferEndpoint: string;
  locationInventoryHistoriesEndpoint: string;
  shipmentProductItemEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.shipmentProductItemEndpoint = `${config.env.serverApiURL}/v1/shipments/pack-item`;
    this.shipmentEndpoint = `${config.env.serverApiURL}/v1/shipments`;
    this.transitTimeEndpoint = `${config.env.serverApiURL}/v1/transit-time/calculate`;
    this.returnsEndpoint = `${config.env.serverApiURL}/v1/returns`;
    this.externalTransferEndpoint = `${config.env.serverApiURL}/v1/external-transfers`;
    this.locationInventoryHistoriesEndpoint = `${config.env.serverApiURL}/v1/location-inventory-histories`;
  }

  //#region SHIPMENT

  public verifyShipmentProduct(shipment: VerifyProduct): Observable<HttpResponse<RuleResponseDto>> {
    console.log('prod');
    return this.httpClient
      .post<RuleResponseDto>(this.shipmentProductItemEndpoint, shipment, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getShipmentByCriteria(criteria?: {}): Observable<HttpResponse<ShipmentDto[]>> {
    return this.httpClient
      .get<ShipmentDto[]>(`${this.shipmentEndpoint}`, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getShipmentById(id: number): Observable<HttpResponse<ShipmentInfoDto>> {
    return this.httpClient
      .get<ShipmentInfoDto[]>(`${this.shipmentEndpoint}/${id}`, {
        params: {},
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  //#region Descriptions
  public getOrderInfoDescriptions(shipmentInfo: ShipmentInfoDto): Description[] {
    return [
      { label: 'order-number.label', value: shipmentInfo?.orderNumber?.toString() },
      {
        label: 'priority.label',
        value: shipmentInfo?.priority,
      },
      {
        label: 'status.label',
        value: shipmentInfo?.status,
      },
      { label: 'labeling-product-category.label', value: shipmentInfo?.productCategory },
    ];
  }

  public getShippingInfoDescriptions(shipmentInfo: ShipmentInfoDto): Description[] {
    return [
      { label: 'shipment-id.label', value: shipmentInfo.id.toString() },
      { label: 'customer-id.label', value: shipmentInfo?.shippingCustomerCode.toString() },
      { label: 'customer-name.label', value: shipmentInfo?.shippingCustomerName },
      {
        label: 'ship-date.label',
        value: shipmentInfo?.shippingDate ? moment(shipmentInfo.shippingDate).format('MM/DD/YYYY') : '',
      },
      {
        label: 'ship-method.label',
        value: shipmentInfo?.shippingMethod,
      },
    ];
  }

  //#endregion

  public createShipment(shipment: ShipmentDto): Observable<HttpResponse<ShipmentDto>> {
    return this.httpClient.post<ShipmentDto>(this.shipmentEndpoint, shipment, { observe: 'response' });
  }

  //#endregion

  //#region TRANSIT TIME

  public currentTime(timezone: string): Observable<CurrentTimeDto> {
    return this.httpClient
      .get<CurrentTimeDto>(`${this.transitTimeEndpoint}/current-time`, {
        params: { timezone },
      })
      .pipe(catchError(this.errorHandler));
  }

  public calculateTransiteTime(dto: TransitTimeRequestDto): Observable<TransitTimeResponseDto> {
    return this.httpClient
      .post<TransitTimeResponseDto>(this.transitTimeEndpoint, dto)
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region RETURNS

  public confirmReturn(dto: ReturnsDto): Observable<ReturnsDto> {
    return this.httpClient.post<ReturnsDto>(this.returnsEndpoint, dto).pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region EXTERNAL TRANSFER

  public createExternalTransfer(dto: ExternalTransferDto): Observable<ExternalTransferDto> {
    return this.httpClient
      .post<ExternalTransferDto>(this.externalTransferEndpoint, dto)
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region INVENTORY HISTORIES

  public getInventoryHistoriesByCriteria(criteria?: { [key: string]: any }): Observable<LocationInventoryHistoryDto[]> {
    return this.httpClient
      .get<LocationInventoryHistoryDto[]>(`${this.locationInventoryHistoriesEndpoint}`, {
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
