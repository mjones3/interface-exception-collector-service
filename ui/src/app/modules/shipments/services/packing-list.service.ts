import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Apollo } from 'apollo-angular';
import { Observable } from 'rxjs';
import { GENERATE_PACKING_LIST_LABEL } from '../graphql/query-defintions/packing-list.graphql';
import { GENERATE_SHIPPING_LABEL } from '../graphql/shipping-label/query-definitions/shipping-label.graphql';
import { PackingListLabelDTO } from '../models/packing-list.model';
import { ShippingLabelDTO } from '../models/shipping-label.model';


@Injectable({
  providedIn: 'root',
})
export class PackingListService {

  constructor(
    private apollo: Apollo
  ) {}

  generatePackingListLabel(shipmentId: number): Observable<ApolloQueryResult<{ generatePackingListLabel: PackingListLabelDTO }>> {
    return this.apollo
      .query<{ generatePackingListLabel: PackingListLabelDTO }>({
        query: GENERATE_PACKING_LIST_LABEL,
        variables: { shipmentId },
      });
  }

  generateShippingLabel(shipmentId: number): Observable<ApolloQueryResult<{ generateShippingLabel: ShippingLabelDTO }>> {
    return this.apollo
      .query<{ generateShippingLabel: ShippingLabelDTO }>({
        query: GENERATE_SHIPPING_LABEL,
        variables: { shipmentId },
      });
  }

}
