import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { MutationResult } from 'apollo-angular';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { Observable } from 'rxjs';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import {
    CREATE_RECOVERED_PLASMA_SHIPMENT,
    GET_PRODUCT_TYPE_OPTIONS,
    productTypeOptionResponse,
} from '../graphql/create-recovered-plasma-shipment.graphql';
import { CreateShipmentRequestDTO } from '../models/recovered-plasma.dto';

@Injectable({
    providedIn: 'root',
})
export class RecoveredPlasmaShipmentService {
    readonly servicePath = '/recoveredplasmashipping/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public getProductTypeOptions(customerCode: string): Observable<
        ApolloQueryResult<{
            findAllProductTypeByCustomer: productTypeOptionResponse;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            GET_PRODUCT_TYPE_OPTIONS,
            { customerCode }
        );
    }

    public createRecoveredPlasmaShipment(
        createShipmentRequest: CreateShipmentRequestDTO
    ): Observable<MutationResult<{ createShipment: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CREATE_RECOVERED_PLASMA_SHIPMENT,
            createShipmentRequest
        );
    }
}
