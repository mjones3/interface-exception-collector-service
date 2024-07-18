import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Apollo } from 'apollo-angular';
import { Observable, throwError } from 'rxjs';
import { LIST_SHIPMENT } from '../../../modules/shipments/graphql/shipment/query-definitions/shipment.graphql';
import { OrderSummaryDto } from '../models/order.dto';


@Injectable({
  providedIn: 'root',
})
export class OrderService {
  apolloClient: Apollo;

  constructor( private apollo: Apollo) {this.apolloClient = apollo;}

  public getOrdersSummaryByCriteria(
    criteria?: {},
    refetch: boolean = false
  ): Observable<ApolloQueryResult<{ listShipments: OrderSummaryDto[] }>> {  
    return this.apolloClient.query<{ listShipments: OrderSummaryDto[] }>({
      query: LIST_SHIPMENT,
      ...(refetch ? { fetchPolicy: 'network-only' } : {}),
    }); 
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
