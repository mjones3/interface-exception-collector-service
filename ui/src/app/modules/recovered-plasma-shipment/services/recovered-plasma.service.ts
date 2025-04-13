import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { LookUpDto } from '@shared';
import { MutationResult } from 'apollo-angular';
import { Observable, Observer } from 'rxjs';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import { PageDTO } from '../../../shared/models/page.model';
import { UseCaseResponseDTO } from '../../../shared/models/use-case-response.dto';
import {
    CREATE_CARTON,
    CreateCartonRequestDTO,
} from '../graphql/mutation-definitions/create-carton.graphql';
import {
    FIND_ALL_CUSTOMERS,
    RecoveredPlasmaCustomerDTO,
} from '../graphql/query-definitions/customer.graphql';
import {
    FIND_ALL_LOCATIONS,
    RecoveredPlasmaLocationDTO,
} from '../graphql/query-definitions/location.graphql';
import { FIND_ALL_LOOKUPS_BY_TYPE } from '../graphql/query-definitions/lookup.graphql';
import {
    RecoveredPlasmaShipmentQueryCommandRequestDTO,
    RecoveredPlasmaShipmentReportDTO,
    SEARCH_RP_SHIPMENT,
} from '../graphql/query-definitions/shipment.graphql';
import {
    FindShipmentRequestDTO,
    RECOVERED_PLASMA_SHIPMENT_DETAILS,
} from '../graphql/query-definitions/shipmentDetails.graphql';
import {
    CartonDTO,
    RecoveredPlasmaShipmentResponseDTO,
} from '../models/recovered-plasma.dto';

@Injectable({
    providedIn: 'root',
})
export class RecoveredPlasmaService {
    readonly servicePath = '/recoveredplasmashipping/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public searchRecoveredPlasmaShipments(
        recoveredPlasmaShipmentQueryCommandRequestDTO: RecoveredPlasmaShipmentQueryCommandRequestDTO
    ): Observable<
        ApolloQueryResult<{
            searchShipment: UseCaseResponseDTO<
                PageDTO<RecoveredPlasmaShipmentReportDTO>
            >;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            SEARCH_RP_SHIPMENT,
            { recoveredPlasmaShipmentQueryCommandRequestDTO }
        );
    }

    public findAllLookupsByType(type: string): Observable<
        ApolloQueryResult<{
            findAllLookupsByType: LookUpDto[];
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_LOOKUPS_BY_TYPE,
            { type }
        );
    }

    public findAllLocations(): Observable<
        ApolloQueryResult<{
            findAllLocations: RecoveredPlasmaLocationDTO[];
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_LOCATIONS
        );
    }

    public findAllCustomers(): Observable<
        ApolloQueryResult<{
            findAllCustomers: RecoveredPlasmaCustomerDTO[];
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_CUSTOMERS
        );
    }

    public createCarton(
        request: CreateCartonRequestDTO
    ): Observable<
        MutationResult<{ createCarton: UseCaseResponseDTO<CartonDTO> }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CREATE_CARTON,
            request
        );
    }

    public checkRecoveredPlasmaFacility(facilityCode: string) {
        let matchFacility;
        return new Observable((observer: Observer<boolean>) => {
            this.findAllLocations().subscribe((res) => {
                matchFacility = res.data.findAllLocations.find(
                    (location) => location.code === facilityCode
                );
                observer.next(!!matchFacility);
                observer.complete();
            });
        });
    }

    public getShipmentById(
        findShipmentCommandDTO: FindShipmentRequestDTO
    ): Observable<
        ApolloQueryResult<{
            findShipmentById: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            RECOVERED_PLASMA_SHIPMENT_DETAILS,
            { findShipmentCommandDTO }
        );
    }
}
