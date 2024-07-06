import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Apollo } from 'apollo-angular';
import { Description } from 'app/shared/models/description.model';
import { EnvironmentConfigService } from 'app/shared/services';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { COMPLETE_SHIPMENT, PACK_ITEM } from '../graphql/shipment/mutation-definitions/shipment.graphql';
import { GET_SHIPMENT_BY_ID } from '../graphql/shipment/query-definitions/shipment.graphql';
import { ShipmentInfoDto, VerifyProductDto } from '../models/shipment-info.dto';

@Injectable({
  providedIn: 'root',
})
export class ShipmentService {

  err = new Error('test')

  constructor(
    private config: EnvironmentConfigService, 
    private apollo: Apollo
  ) {}

  //#region SHIPMENT

  public completeShipment(inputs: any, refetch: boolean = false) {
    return this.apollo
      .mutate({
        mutation: COMPLETE_SHIPMENT,
        variables: { shipmentId: inputs.shipmentId, employeeId: inputs.employeeId },
      })
      .pipe(catchError(this.errorHandler));
  }

  public verifyShipmentProduct(shipment: VerifyProductDto) {
    return this.apollo
      .mutate({
        mutation: PACK_ITEM,
        variables: {
          shipmentItemId: shipment.shipmentItemId,
          locationCode: shipment.locationCode,
          unitNumber: shipment.unitNumber,
          employeeId: shipment.employeeId,
          productCode: shipment.productCode,
          visualInspection: shipment.visualInspection,
        },
      })
      .pipe(catchError(this.errorHandler));
  }



  public getShipmentById(
    shipmentId: number,
    refetch: boolean = false
  ): Observable<ApolloQueryResult<{ getShipmentDetailsById: ShipmentInfoDto }>> {
    return this.apollo.query<{ getShipmentDetailsById: ShipmentInfoDto }>({
      query: GET_SHIPMENT_BY_ID,
      variables: { shipmentId },
      ...(refetch ? { fetchPolicy: 'network-only' } : {}),
    });
  }

  //#region Descriptions
  public getOrderInfoDescriptions(shipmentInfo: ShipmentInfoDto): Description[] {
    return [
      { label: 'order-number', value: shipmentInfo?.orderNumber?.toString() },
      {
        label: 'priority',
        value: shipmentInfo?.priority,
      },
      { label: 'labeling-product-category', value: shipmentInfo?.productCategory },
    ];
  }

  public getShippingInfoDescriptions(shipmentInfo: ShipmentInfoDto): Description[] {
    return [
      { label: 'shipment-id', value: shipmentInfo.id.toString() },
      { label: 'customer-id', value: shipmentInfo?.shippingCustomerCode.toString() },
      { label: 'customer-name', value: shipmentInfo?.shippingCustomerName },
      { label: 'status', value: shipmentInfo?.status },
      {
        label: 'ship-method',
        value: shipmentInfo?.shippingMethod,
      },
    ];
  }

  //#endregion

  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(() => error);
  }
}
