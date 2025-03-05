import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { MutationResult } from 'apollo-angular';
import { Observable } from 'rxjs';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import { SEARCH_ORDER_CRITERIA } from '../../shipments/graphql/order/query-definitions/search-order-criteria.graphsql';
import { SEARCH_ORDERS } from '../../shipments/graphql/order/query-definitions/search-orders.graphql';
import {
    COMPLETE_ORDER,
    CompleteOrderCommandDTO,
} from '../graphql/mutation-definitions/complete-order.graphql';
import {
    GENERATE_PICK_LIST,
    PickListDTO,
} from '../graphql/mutation-definitions/generate-pick-list.graphql';
import {
    FIND_ORDER_SHIPMENT_BY_ORDER_ID,
    GET_ORDER_BY_ID,
    OrderShipmentDTO,
} from '../graphql/query-definitions/order-details.graphql';
import { Notification } from '../models/notification.dto';
import { OrderCriteriaDTO } from '../models/order-criteria.model';
import { OrderDetailsDTO } from '../models/order-details.dto';
import { PageDTO } from '../models/page.model';
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
    ): Observable<
        ApolloQueryResult<{ searchOrders: PageDTO<OrderReportDTO> }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            SEARCH_ORDERS,
            { orderQueryCommandDTO }
        );
    }

    public searchOrderCriteria(): Observable<
        ApolloQueryResult<{ searchOrderCriteria: OrderCriteriaDTO }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            SEARCH_ORDER_CRITERIA
        );
    }

    public getOrderById(orderId: number): Observable<
        ApolloQueryResult<{
            findOrderById: {
                notifications: Notification[];
                data: OrderDetailsDTO;
            };
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            GET_ORDER_BY_ID,
            { orderId }
        );
    }

    public generatePickList(
        orderId: number,
        skipInventoryUnavailable = false
    ): Observable<
        MutationResult<{
            generatePickList: {
                notifications: Notification[];
                data: PickListDTO;
            };
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            GENERATE_PICK_LIST,
            { orderId, skipInventoryUnavailable }
        );
    }

    public findOrderShipmentByOrderId(orderId: number): Observable<
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

    public completeOrder(command: CompleteOrderCommandDTO): Observable<
        MutationResult<{
            completeOrder: {
                notifications: Notification[];
                data: OrderDetailsDTO;
            };
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            COMPLETE_ORDER,
            command
        );
    }
}
