import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Apollo } from 'apollo-angular';
import { Observable } from 'rxjs';
import { SEARCH_ORDERS } from '../../shipments/graphql/order/query-definitions/search-orders.graphql';
import {
    OrderQueryCommandDTO,
    OrderReportDTO,
} from '../order/models/search-order.model';

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

}
