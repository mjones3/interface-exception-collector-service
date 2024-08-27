import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Description } from '@shared';
import { MutationResult } from 'apollo-angular';
import { Observable } from 'rxjs';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import { SEARCH_ORDERS } from '../../shipments/graphql/order/query-definitions/search-orders.graphql';
import {
    GENERATE_PICK_LIST,
    PickListDTO,
} from '../graphql/mutation-definitions/generate-pick-list.graphql';
import {
    FIND_ORDER_SHIPMENT_BY_ORDER_ID,
    GET_ORDER_BY_ID,
    OrderShipmentDTO,
} from '../graphql/query-definitions/order-details.graphql';
import { OrderDetailsDTO } from '../models/order-details.dto';
import {
    OrderQueryCommandDTO,
    OrderReportDTO,
} from '../models/search-order.model';

@Injectable({
    providedIn: 'root',
})
export class OrderService {
    readonly servicePath = '/order/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public searchOrders(
        orderQueryCommandDTO: OrderQueryCommandDTO
    ): Observable<ApolloQueryResult<{ searchOrders: OrderReportDTO[] }>> {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            SEARCH_ORDERS,
            { orderQueryCommandDTO }
        );
    }

    public getOrderById(
        orderId: number
    ): Observable<ApolloQueryResult<{ findOrderById: OrderDetailsDTO }>> {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            GET_ORDER_BY_ID,
            { orderId }
        );
    }

    public generatePickList(
        orderId: number
    ): Observable<MutationResult<{ generatePickList: PickListDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            GENERATE_PICK_LIST,
            { orderId }
        );
    }

    public findOrderShipmentByOrderId(
        orderId: number
    ): Observable<
        ApolloQueryResult<{
            findOrderShipmentByOrderId: OrderShipmentDTO | null;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ORDER_SHIPMENT_BY_ORDER_ID,
            { orderId }
        );
    }

    public getOrderInfoDescriptions(orderInfo: OrderDetailsDTO): Description[] {
        return [
            {
                label: 'BioPro Order Number',
                value: orderInfo?.orderNumber?.toString(),
            },
            {
                label: 'External order ID',
                value: orderInfo?.externalId?.toString(),
            },
            {
                label: 'Priority',
                value: orderInfo?.priority.toString(),
            },
            { label: 'Status', value: orderInfo?.status.toString() },
        ];
    }

    public getShippingInfoDescriptions(
        orderInfo: OrderDetailsDTO
    ): Description[] {
        return [
            {
                label: 'Customer Code',
                value: orderInfo?.shippingCustomerCode.toString(),
            },
            {
                label: 'Customer Name',
                value: orderInfo?.shippingCustomerName.toString(),
            },
            {
                label: 'Shipping Method',
                value: orderInfo?.shippingMethod.toString(),
            },
        ];
    }

    public getBillingInfoDescriptions(
        orderInfo: OrderDetailsDTO
    ): Description[] {
        return [
            {
                label: 'Billing Customer Code',
                value: orderInfo?.billingCustomerCode.toString(),
            },
            {
                label: 'Billing Customer Name',
                value: orderInfo?.billingCustomerName.toString(),
            },
        ];
    }
}
