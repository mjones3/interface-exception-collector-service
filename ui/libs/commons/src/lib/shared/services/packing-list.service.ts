import { Injectable } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { Observable } from 'rxjs';
import { GENERATE_PACKING_LIST_LABEL } from '../../schemas/graphql/packing-list/query-defintions/packing-list.graphql';

@Injectable({
  providedIn: 'root',
})
export class PackingListService {

  constructor(
    private apollo: Apollo
  ) {}

  getLabel(shipmentId: number): Observable<any> {
    return this.apollo
      .query<any>({
        query: GENERATE_PACKING_LIST_LABEL,
        variables: { shipmentId },
      });
  }

}
