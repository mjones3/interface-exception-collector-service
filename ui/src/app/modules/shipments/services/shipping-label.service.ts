import { Injectable } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client';
import { ShippingLabelDTO } from '../models/shipping-label.model';
import { GENERATE_SHIPPING_LABEL } from '../graphql/query-defintions/shipping-label.graphql';

@Injectable({
  providedIn: 'root',
})
export class ShippingLabelService {

  constructor(
    private apollo: Apollo
  ) {}

  public generate(
      shipmentId: number,
      refetch = false,
  ): Observable<ApolloQueryResult<{ generateShippingLabel: ShippingLabelDTO }>> {
    return this.apollo
      .query<{ generateShippingLabel: ShippingLabelDTO }>({
        query: GENERATE_SHIPPING_LABEL,
        variables: { shipmentId },
        ...(refetch ? { fetchPolicy: 'network-only' } : {}),
      });
  }

}
