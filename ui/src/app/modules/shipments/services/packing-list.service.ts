import { Injectable } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client';
import { PackingListLabelDTO } from '../models/packing-list.model';
import { GENERATE_PACKING_LIST_LABEL } from '../graphql/query-defintions/packing-list.graphql';

@Injectable({
  providedIn: 'root',
})
export class PackingListService {

  constructor(
    private apollo: Apollo
  ) {}

  public generate(
      shipmentId: number,
      refetch = false,
  ): Observable<ApolloQueryResult<{ generatePackingListLabel: PackingListLabelDTO }>> {
    return this.apollo
      .query<{ generatePackingListLabel: PackingListLabelDTO }>({
        query: GENERATE_PACKING_LIST_LABEL,
        variables: { shipmentId },
        ...(refetch ? { fetchPolicy: 'network-only' } : {}),
      });
  }

}
