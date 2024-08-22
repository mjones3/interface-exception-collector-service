import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client';
import { ShippingLabelDTO } from '../models/shipping-label.model';
import { GENERATE_SHIPPING_LABEL } from '../graphql/query-defintions/shipping-label.graphql';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';

@Injectable({
  providedIn: 'root',
})
export class ShippingLabelService {

    readonly servicePath = '/shipping/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public generate(shipmentId: number)
        : Observable<ApolloQueryResult<{ generateShippingLabel: ShippingLabelDTO }>> {
        return this.dynamicGraphqlPathService
            .executeQuery(this.servicePath, GENERATE_SHIPPING_LABEL, { shipmentId });
    }

}
