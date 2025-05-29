import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { MutationResult } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { Observable } from 'rxjs';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import {
    CreateShipmentRequestDTO,
    RecoveredPlasmaShipmentResponseDTO,
} from '../models/recovered-plasma.dto';
import { GET_PRODUCT_TYPE_OPTIONS, productTypeOptionResponse } from '../graphql/query-definitions/create-rp-shipment.graphql';
import { CREATE_RECOVERED_PLASMA_SHIPMENT } from '../graphql/mutation-definitions/create-recovered-plasma-shipment.graphql';
import { FIND_SHIPMENT_HISTORY_BY_ID, ShipmentHistoryDTO } from '../graphql/query-definitions/shipment-comments-history.graphql';
import { MODIFY_RECOVERED_PLASMA_SHIPMENT, ModifyShipmentRequestDTO } from '../graphql/mutation-definitions/modify-shipment.graphql';

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
    ): Observable<
        MutationResult<{
            createShipment: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CREATE_RECOVERED_PLASMA_SHIPMENT,
            createShipmentRequest
        );
    }


    public editRecoveredPlasmaShipment(
        modifyShipmentRequest: ModifyShipmentRequestDTO
    ): Observable<
        MutationResult<{
            modifyShipment: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            MODIFY_RECOVERED_PLASMA_SHIPMENT,
            modifyShipmentRequest
        );
    }

    public getShipmentHistory(
        shipmentId: number
    ): Observable<
    ApolloQueryResult<{
            findAllShipmentHistoryByShipmentId: ShipmentHistoryDTO;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_SHIPMENT_HISTORY_BY_ID,
            {shipmentId}
        );
    }
}
