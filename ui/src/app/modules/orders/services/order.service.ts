import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Description } from '@shared';
import { Apollo } from 'apollo-angular';
import { Observable } from 'rxjs';
import { SEARCH_ORDERS } from '../../shipments/graphql/order/query-definitions/search-orders.graphql';
import { GET_ORDER_BY_ID } from '../graphql/order-details.graphql';
import { OrderDetailsDto } from '../models/order-details.dto';
import {
    OrderQueryCommandDTO,
    OrderReportDTO,
} from '../models/search-order.model';

@Injectable({
    providedIn: 'root',
})
export class OrderService {
    constructor(private apollo: Apollo) {}

    public searchOrders(
        commandQuery: OrderQueryCommandDTO,
        refetch = false
    ): Observable<ApolloQueryResult<{ searchOrders: OrderReportDTO[] }>> {
        return this.apollo.query<{ searchOrders: OrderReportDTO[] }>({
            query: SEARCH_ORDERS,
            variables: {
                orderQueryCommandDTO: commandQuery,
            },
            ...(refetch ? { fetchPolicy: 'network-only' } : {}),
        });
    }

    public getOrderById(
        orderId: number,
        refetch = false
    ): Observable<ApolloQueryResult<{ findOrderById: OrderDetailsDto }>> {
        return this.apollo.query<{ findOrderById: OrderDetailsDto }>({
            query: GET_ORDER_BY_ID,
            variables: { orderId },
            ...(refetch ? { fetchPolicy: 'network-only' } : {}),
        });
    }

    public getOrderInfoDescriptions(orderInfo: OrderDetailsDto): Description[] {
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
        orderInfo: OrderDetailsDto
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
        orderInfo: OrderDetailsDto
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
