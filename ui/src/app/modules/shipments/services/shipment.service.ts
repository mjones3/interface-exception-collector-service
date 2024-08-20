import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Description } from 'app/shared/models/description.model';
import { Observable } from 'rxjs';
import { COMPLETE_SHIPMENT, PACK_ITEM } from '../graphql/shipment/mutation-definitions/shipment.graphql';
import { GET_SHIPMENT_BY_ID, LIST_SHIPMENTS } from '../graphql/shipment/query-definitions/shipment.graphql';
import { ShipmentDetailResponseDTO, ShipmentResponseDTO, VerifyProductDTO } from '../models/shipment-info.dto';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import { RuleResponseDTO } from '../../../shared/models/rule.model';
import { MutationResult } from 'apollo-angular';

@Injectable({
    providedIn: 'root',
})
export class ShipmentService {

    readonly servicePath = '/shipping/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public listShipments()
        : Observable<ApolloQueryResult<{ listShipments: ShipmentResponseDTO[] }>> {
        return this.dynamicGraphqlPathService
            .executeQuery(this.servicePath, LIST_SHIPMENTS);
    }

    public completeShipment(inputs: { shipmentId: number, employeeId: string })
        : Observable<MutationResult<{ completeShipment: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService
            .executeMutation(this.servicePath, COMPLETE_SHIPMENT, inputs);
    }

    public verifyShipmentProduct(verifyProductDTO: VerifyProductDTO)
        : Observable<MutationResult<{ packItem: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService
            .executeMutation(this.servicePath, PACK_ITEM, verifyProductDTO);
    }

    public getShipmentById(shipmentId: number)
        : Observable<ApolloQueryResult<{ getShipmentDetailsById: ShipmentDetailResponseDTO }>> {
        return this.dynamicGraphqlPathService
            .executeQuery(this.servicePath, GET_SHIPMENT_BY_ID, { shipmentId });
    }

    //#region Descriptions
    public getOrderInfoDescriptions(
        shipmentInfo: ShipmentDetailResponseDTO
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
        shipmentInfo: ShipmentDetailResponseDTO
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

}
