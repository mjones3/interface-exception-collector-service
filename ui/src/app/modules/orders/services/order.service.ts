import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Apollo } from 'apollo-angular';
import { LIST_SHIPMENT } from 'app/modules/shipments/graphql/shipment/query-definitions/shipment.graphql';
import { Observable } from 'rxjs';
import { OrderSummaryDto } from '../models/order.dto';
import { OrderQueryCommandDTO, OrderReportDTO } from '../order/models/search-order.model';
import { SEARCH_ORDERS } from '../../shipments/graphql/order/query-definitions/search-orders.graphql';

@Injectable({
    providedIn: 'root',
})
export class OrderService {
    apolloClient: Apollo;

    constructor(private apollo: Apollo) {
        this.apolloClient = apollo;
    }

    public getOrdersSummaryByCriteria(
        criteria?: object,
        refetch = false
    ): Observable<ApolloQueryResult<{ listShipments: OrderSummaryDto[] }>> {
        return this.apolloClient.query<{ listShipments: OrderSummaryDto[] }>({
            query: LIST_SHIPMENT,
            ...(refetch ? { fetchPolicy: 'network-only' } : {}),
        });
    }

    public searchOrders(commandQuery: OrderQueryCommandDTO, refetch = false
    ) : Observable<ApolloQueryResult<{ searchOrders: OrderReportDTO[] }>> {
        return this.apolloClient.query<{ searchOrders: OrderReportDTO[] }>({
            query: SEARCH_ORDERS,
            variables: {
                orderQueryCommandDTO: commandQuery,
            },
            ...(refetch ? { fetchPolicy: 'network-only' } : {}),
        });
    }

}
