import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Apollo } from 'apollo-angular';
import { LIST_SHIPMENT } from 'app/modules/shipments/graphql/shipment/query-definitions/shipment.graphql';
import { Observable } from 'rxjs';
import { OrderSummaryDto } from '../models/order.dto';

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
}
