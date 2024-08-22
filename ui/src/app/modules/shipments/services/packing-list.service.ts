import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client';
import { PackingListLabelDTO } from '../models/packing-list.model';
import { GENERATE_PACKING_LIST_LABEL } from '../graphql/query-defintions/packing-list.graphql';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';

@Injectable({
  providedIn: 'root',
})
export class PackingListService {

    readonly servicePath = '/shipping/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public generate(shipmentId: number)
        : Observable<ApolloQueryResult<{ generatePackingListLabel: PackingListLabelDTO }>> {
        return this.dynamicGraphqlPathService
            .executeQuery(this.servicePath, GENERATE_PACKING_LIST_LABEL, { shipmentId });
    }

}
