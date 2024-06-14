import { Injectable } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { Observable } from 'rxjs';
import { GENERATE_PACKING_LIST_LABEL } from '../../schemas/graphql/packing-list/query-defintions/packing-list.graphql';
import { ApolloQueryResult } from '@apollo/client';
import { PackingListLabelDTO, ShippingLabelDTO } from '../models';
import { GENERATE_SHIPPING_LABEL } from '../../schemas/graphql/shipping-label/query-definitions/shipping-label.graphql';

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
