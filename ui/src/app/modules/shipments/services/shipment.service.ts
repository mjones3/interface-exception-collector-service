import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Apollo } from 'apollo-angular';
import { Description } from 'app/shared/models/description.model';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
    COMPLETE_SHIPMENT,
    PACK_ITEM,
} from '../graphql/shipment/mutation-definitions/shipment.graphql';
import {
    GET_SHIPMENT_BY_ID,
    LIST_SHIPMENT,
} from '../graphql/shipment/query-definitions/shipment.graphql';
import { ShipmentInfoDto, VerifyProductDto } from '../models/shipment-info.dto';

@Injectable({
    providedIn: 'root',
})
export class ShipmentService {
    err = new Error('test');

    constructor(private apollo: Apollo) {}

    //#region SHIPMENT

    public listShipments(
        criteria?: object,
        refetch = false
    ): Observable<ApolloQueryResult<{ listShipments: [] }>> {
        return this.apollo.query<{ listShipments: [] }>({
            query: LIST_SHIPMENT,
            ...(refetch ? { fetchPolicy: 'network-only' } : {}),
        });
    }

    public completeShipment(inputs: any, refetch = false) {
        return this.apollo
            .mutate({
                mutation: COMPLETE_SHIPMENT,
                variables: {
                    shipmentId: inputs.shipmentId,
                    employeeId: inputs.employeeId,
                },
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
        refetch = false
    ): Observable<
        ApolloQueryResult<{ getShipmentDetailsById: ShipmentInfoDto }>
    > {
        return this.apollo.query<{ getShipmentDetailsById: ShipmentInfoDto }>({
            query: GET_SHIPMENT_BY_ID,
            variables: { shipmentId },
            ...(refetch ? { fetchPolicy: 'network-only' } : {}),
        });
    }

    //#region Descriptions
    public getOrderInfoDescriptions(
        shipmentInfo: ShipmentInfoDto
    ): Description[] {
        return [
            {
                label: 'Order Number',
                value: shipmentInfo?.orderNumber?.toString(),
            },
            {
                label: 'Priority',
                value: shipmentInfo?.priority,
            },
            {
                label: 'Labeling Product Category',
                value: shipmentInfo?.productCategory,
            },
        ];
    }

    public getShippingInfoDescriptions(
        shipmentInfo: ShipmentInfoDto
    ): Description[] {
        return [
            { label: 'Shipment Id', value: shipmentInfo.id.toString() },
            {
                label: 'Customer Id',
                value: shipmentInfo?.shippingCustomerCode.toString(),
            },
            {
                label: 'Customer Name',
                value: shipmentInfo?.shippingCustomerName,
            },
            { label: 'Status', value: shipmentInfo?.status },
            {
                label: 'Shipping Method',
                value: shipmentInfo?.shippingMethod,
            },
        ];
    }

    //#endregion

    public errorHandler(error: HttpErrorResponse): Observable<any> {
        return throwError(() => error);
    }
}
