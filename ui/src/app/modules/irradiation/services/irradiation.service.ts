import { Injectable } from '@angular/core';
import {DynamicGraphqlPathService} from "../../../core/services/dynamic-graphql-path.service";
import {OrderQueryCommandDTO, OrderReportDTO} from "../../orders/models/search-order.model";
import {Observable} from "rxjs";
import {ApolloQueryResult} from "@apollo/client";
import {PageDTO} from "../../../shared/models/page.model";
import {SEARCH_ORDERS} from "../../shipments/graphql/order/query-definitions/search-orders.graphql";
import {OrderCriteriaDTO} from "../../orders/models/order-criteria.model";
import {SEARCH_ORDER_CRITERIA} from "../../shipments/graphql/order/query-definitions/search-order-criteria.graphsql";
import {Notification} from "../../orders/models/notification.dto";
import {OrderDetailsDTO} from "../../orders/models/order-details.dto";
import {
    FIND_ORDER_SHIPMENT_BY_ORDER_ID,
    GET_ORDER_BY_ID,
    OrderShipmentDTO
} from "../../orders/graphql/query-definitions/order-details.graphql";
import {MutationResult} from "apollo-angular";
import {GENERATE_PICK_LIST, PickListDTO} from "../../orders/graphql/mutation-definitions/generate-pick-list.graphql";
import {
    COMPLETE_ORDER,
    CompleteOrderCommandDTO
} from "../../orders/graphql/mutation-definitions/complete-order.graphql";
import {DeviceDTO, SubmitCentrifugationBatchRequestDTO, UnitNumberRequestDTO} from "../models/model";

@Injectable({
  providedIn: 'root'
})
export class IrradiationService {

    readonly servicePath = '/order/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    loadDeviceById(deviceDto: DeviceDTO) {
        return null;
    }

    scanOrEnterUnitNumber(unitNumberRequestDTO: UnitNumberRequestDTO) {
       return null;
    }

    submitCentrifugationBatch(dto: SubmitCentrifugationBatchRequestDTO) {
       return null;
    }

    // public searchOrders(orderQueryCommandDTO: OrderQueryCommandDTO): Observable<
    //     ApolloQueryResult<{ searchOrders: PageDTO<OrderReportDTO> }>
    // > {
    //     return this.dynamicGraphqlPathService.executeQuery(
    //         this.servicePath,
    //         SEARCH_ORDERS,
    //         { orderQueryCommandDTO }
    //     );
    // }
    //
    // public searchOrderCriteria(): Observable<
    //     ApolloQueryResult<{ searchOrderCriteria: OrderCriteriaDTO }>
    // > {
    //     return this.dynamicGraphqlPathService.executeQuery(
    //         this.servicePath,
    //         SEARCH_ORDER_CRITERIA
    //     );
    // }
    //
    // public getOrderById(orderId: number): Observable<
    //     ApolloQueryResult<{
    //         findOrderById: {
    //             notifications: Notification[];
    //             data: OrderDetailsDTO;
    //         };
    //     }>
    // > {
    //     return this.dynamicGraphqlPathService.executeQuery(
    //         this.servicePath,
    //         GET_ORDER_BY_ID,
    //         { orderId }
    //     );
    // }
    //
    // public generatePickList(
    //     orderId: number,
    //     skipInventoryUnavailable = false
    // ): Observable<
    //     MutationResult<{
    //         generatePickList: {
    //             notifications: Notification[];
    //             data: PickListDTO;
    //         };
    //     }>
    // > {
    //     return this.dynamicGraphqlPathService.executeMutation(
    //         this.servicePath,
    //         GENERATE_PICK_LIST,
    //         { orderId, skipInventoryUnavailable }
    //     );
    // }
    //
    //
    // public completeOrder(command: CompleteOrderCommandDTO): Observable<
    //     MutationResult<{
    //         completeOrder: {
    //             notifications: Notification[];
    //             data: OrderDetailsDTO;
    //         };
    //     }>
    // > {
    //     return this.dynamicGraphqlPathService.executeMutation(
    //         this.servicePath,
    //         COMPLETE_ORDER,
    //         command
    //     );
    // }
}
